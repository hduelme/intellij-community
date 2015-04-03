/*
 * Copyright 2010-2015 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jetbrains.kotlin.idea.codeInsight

import com.intellij.openapi.editor.RangeMarker
import com.intellij.openapi.editor.Editor
import com.intellij.codeInsight.daemon.impl.CollectHighlightsUtil
import org.jetbrains.kotlin.name.FqName
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Ref
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.application.ApplicationManager
import java.awt.datatransfer.Transferable
import com.intellij.codeInsight.CodeInsightSettings
import com.intellij.codeInsight.editorActions.CopyPastePostProcessor
import com.intellij.openapi.util.TextRange
import com.intellij.psi.util.PsiTreeUtil
import org.jetbrains.kotlin.types.ErrorUtils
import org.jetbrains.kotlin.idea.conversion.copy.*
import java.awt.datatransfer.UnsupportedFlavorException
import java.io.IOException
import com.intellij.openapi.diagnostic.Logger
import org.jetbrains.kotlin.idea.imports.*
import org.jetbrains.kotlin.psi.psiUtil.getReceiverExpression
import com.intellij.openapi.progress.ProcessCanceledException
import org.jetbrains.kotlin.idea.references.*
import org.jetbrains.kotlin.descriptors.*
import org.jetbrains.kotlin.resolve.QualifiedExpressionResolver.LookupMode
import org.jetbrains.kotlin.resolve.*
import org.jetbrains.kotlin.idea.caches.resolve.*
import org.jetbrains.kotlin.idea.util.*
import org.jetbrains.kotlin.utils.*
import org.jetbrains.kotlin.idea.codeInsight.shorten.performDelayedShortening
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.resolve.descriptorUtil.*
import com.intellij.psi.*
import java.util.*

//NOTE: this class is based on CopyPasteReferenceProcessor and JavaCopyPasteReferenceProcessor
public class KotlinCopyPasteReferenceProcessor() : CopyPastePostProcessor<KotlinReferenceTransferableData>() {
    private val LOG = Logger.getInstance(javaClass<KotlinCopyPasteReferenceProcessor>())

    private val IGNORE_REFERENCES_INSIDE: Array<Class<out JetElement>?> = array(
            javaClass<JetImportDirective>(),
            javaClass<JetPackageDirective>()
    )

    override fun extractTransferableData(content: Transferable): List<KotlinReferenceTransferableData> {
        if (CodeInsightSettings.getInstance().ADD_IMPORTS_ON_PASTE != CodeInsightSettings.NO) {
            try {
                val flavor = KotlinReferenceData.dataFlavor ?: return listOf()
                val data = content.getTransferData(flavor) as? KotlinReferenceTransferableData ?: return listOf()
                // copy to prevent changing of original by convertLineSeparators
                return listOf(data.clone())
            }
            catch (ignored: UnsupportedFlavorException) {
            }
            catch (ignored: IOException) {
            }
        }

        return listOf()
    }

    override fun collectTransferableData(
            file: PsiFile,
            editor: Editor,
            startOffsets: IntArray,
            endOffsets: IntArray
    ): List<KotlinReferenceTransferableData> {
        if (file !is JetFile || DumbService.getInstance(file.getProject()).isDumb()) return listOf()

        val collectedData = try {
            collectReferenceData(file, startOffsets, endOffsets)
        }
        catch (e: ProcessCanceledException) {
            // supposedly analysis can only be canceled from another thread
            // do not log ProcessCanceledException as it is rethrown by IdeaLogger and code won't be copied
            LOG.error("ProcessCanceledException while analyzing references in ${file.getName()}. References can't be processed.")
            return listOf()
        }
        catch (e: Throwable) {
            LOG.error("Exception in processing references for copy paste in file ${file.getName()}}", e)
            return listOf()
        }

        if (collectedData.isEmpty()) return listOf()

        return listOf(KotlinReferenceTransferableData(collectedData.copyToArray()))
    }

    public fun collectReferenceData(
            file: JetFile,
            startOffsets: IntArray,
            endOffsets: IntArray
    ): List<KotlinReferenceData> {
        val result = ArrayList<KotlinReferenceData>()
        for (range in toTextRanges(startOffsets, endOffsets)) {
            for (element in CollectHighlightsUtil.getElementsInRange(file, range.start, range.end)) {
                result.addReferenceDataFromElement(element, file, range.start, startOffsets, endOffsets)
            }
        }
        return result
    }

    private fun MutableCollection<KotlinReferenceData>.addReferenceDataFromElement(
            element: PsiElement,
            file: JetFile,
            startOffset: Int,
            startOffsets: IntArray,
            endOffsets: IntArray
    ) {

        if (PsiTreeUtil.getParentOfType(element, *IGNORE_REFERENCES_INSIDE) != null) return

        val reference = element.getReference() as? JetReference ?: return

        val descriptors = reference.resolveToDescriptors((element as JetElement).analyze()) //TODO: we could use partial body resolve for all references together
        //check whether this reference is unambiguous
        if (reference !is JetMultiReference<*> && descriptors.size() > 1) return

        for (descriptor in descriptors) {
            val declarations = DescriptorToSourceUtilsIde.getAllDeclarations(file.getProject(), descriptor)
            val declaration = declarations.singleOrNull()
            if (declaration != null && declaration.isInCopiedArea(file, startOffsets, endOffsets)) continue

            if (!descriptor.isExtension) {
                if (element !is JetNameReferenceExpression) continue
                if (element.getIdentifier() == null) continue // skip 'this' etc
                if (element.getReceiverExpression() != null) continue
            }

            val fqName = descriptor.importableFqName ?: continue
            if (!descriptor.canBeReferencedViaImport()) continue

            val kind = referenceDataKind(descriptor) ?: continue
            add(KotlinReferenceData(element.range.start - startOffset, element.range.end - startOffset, fqName.asString(), kind))
        }
    }

    private fun referenceDataKind(descriptor: DeclarationDescriptor): KotlinReferenceData.Kind? {
        return when (descriptor.getImportableDescriptor()) {
            is ClassDescriptor ->
                KotlinReferenceData.Kind.CLASS

            is PackageViewDescriptor ->
                KotlinReferenceData.Kind.PACKAGE

            is CallableDescriptor ->
                if (descriptor.isExtension) KotlinReferenceData.Kind.EXTENSION_CALLABLE else KotlinReferenceData.Kind.NON_EXTENSION_CALLABLE

            else ->
                null
        }
    }

    private data class ReferenceToRestoreData(
            val reference: JetReference,
            val refData: KotlinReferenceData
    )

    override fun processTransferableData (
            project: Project,
            editor: Editor,
            bounds: RangeMarker,
            caretOffset: Int,
            indented: Ref<Boolean>,
            values: List<KotlinReferenceTransferableData>
    ) {
        if (DumbService.getInstance(project).isDumb()) return

        val document = editor.getDocument()
        val file = PsiDocumentManager.getInstance(project).getPsiFile(document)
        if (file !is JetFile) return

        assert(values.size() == 1)
        val referenceData = values.single().data

        processReferenceData(project, file, bounds.getStartOffset(), referenceData)
    }

    public fun processReferenceData(project: Project, file: JetFile, boundStart: Int, referenceData: Array<KotlinReferenceData>) {
        PsiDocumentManager.getInstance(project).commitAllDocuments()

        val referencesPossibleToRestore = findReferencesToRestore(file, boundStart, referenceData)

        val selectedReferencesToRestore = showRestoreReferencesDialog(project, referencesPossibleToRestore)
        if (selectedReferencesToRestore.isEmpty()) return

        ApplicationManager.getApplication()!!.runWriteAction(Runnable {
            restoreReferences(selectedReferencesToRestore, file)
        })
    }

    private fun findReferencesToRestore(file: PsiFile, boundStart: Int, referenceData: Array<out KotlinReferenceData>): List<ReferenceToRestoreData> {
        if (file !is JetFile) return listOf()

        return referenceData.map {
            val referenceElement = findReference(it, file, boundStart)
            if (referenceElement != null)
                createReferenceToRestoreData(referenceElement, it)
            else
                null
        }.filterNotNull()
    }

    private fun findReference(data: KotlinReferenceData, file: JetFile, boundStart: Int): JetElement? {
        val startOffset = data.startOffset + boundStart
        val endOffset = data.endOffset + boundStart
        val element = file.findElementAt(startOffset)
        val desiredRange = TextRange(startOffset, endOffset)
        var expression = element
        while (expression != null) {
            val range = expression!!.range
            if (range == desiredRange && expression!!.getReference() != null) {
                return expression as? JetElement
            }
            if (range in desiredRange) {
                expression = expression!!.getParent()
            }
            else {
                return null
            }
        }
        return null
    }

    private fun createReferenceToRestoreData(element: JetElement, refData: KotlinReferenceData): ReferenceToRestoreData? {
        val reference = element.getReference() as? JetReference ?: return null
        val referencedDescriptors = try {
            reference.resolveToDescriptors(element.analyze()) //TODO: we could use partial body resolve for all references together
        }
        catch (e: Throwable) {
            LOG.error("Failed to analyze reference (${element.getText()}) after copy paste", e)
            return null
        }
        val referencedFqNames = referencedDescriptors
                .filterNot { ErrorUtils.isError(it) }
                .map { it.importableFqName }
                .filterNotNull()
        val originalFqName = FqName(refData.fqName)
        val referencesSame = referencedFqNames.any { it == originalFqName }
        val conflict = referencedFqNames.any { it != originalFqName }
        if (referencesSame && !conflict) return null
        return ReferenceToRestoreData(reference, refData)
    }

    private fun restoreReferences(referencesToRestore: Collection<ReferenceToRestoreData>, file: JetFile) {
        val importHelper = ImportInsertHelper.getInstance(file.getProject())
        val smartPointerManager = SmartPointerManager.getInstance(file.getProject())

        [data] class BindingRequest(
                val pointer: SmartPsiElementPointer<JetSimpleNameExpression>,
                val fqName: FqName
        )

        val bindingRequests = ArrayList<BindingRequest>()
        val extensionsToImport = ArrayList<CallableDescriptor>()
        for ((reference, refData) in referencesToRestore) {
            val fqName = FqName(refData.fqName)

            if (refData.kind != KotlinReferenceData.Kind.EXTENSION_CALLABLE && reference is JetSimpleNameReference) {
                val pointer = smartPointerManager.createSmartPsiElementPointer(reference.getElement(), file)
                bindingRequests.add(BindingRequest(pointer, fqName))
            }

            if (refData.kind == KotlinReferenceData.Kind.EXTENSION_CALLABLE) {
                extensionsToImport.addIfNotNull(findCallableToImport(fqName, file))
            }
        }

        for (descriptor in extensionsToImport) {
            importHelper.importDescriptor(file, descriptor)
        }
        for ((pointer, fqName) in bindingRequests) {
            val reference = pointer.getElement().getReference() as JetSimpleNameReference
            reference.bindToFqName(fqName, JetSimpleNameReference.ShorteningMode.DELAYED_SHORTENING)
        }
        performDelayedShortening(file.getProject())
    }

    private fun findCallableToImport(fqName: FqName, file: JetFile): CallableDescriptor? {
        val importDirective = JetPsiFactory(file.getProject()).createImportDirective(ImportPath(fqName, false))
        val moduleDescriptor = file.getResolutionFacade().findModuleDescriptor(file)
        val scope = JetModuleUtil.getSubpackagesOfRootScope(moduleDescriptor)
        val descriptors = QualifiedExpressionResolver()
                .processImportReference(importDirective, scope, scope, BindingTraceContext(), LookupMode.EVERYTHING)
                .getAllDescriptors()
                .filterIsInstance<CallableDescriptor>()
        return descriptors.singleOrNull()
    }

    private fun showRestoreReferencesDialog(project: Project, referencesToRestore: List<ReferenceToRestoreData>): Collection<ReferenceToRestoreData> {
        val shouldShowDialog = CodeInsightSettings.getInstance().ADD_IMPORTS_ON_PASTE == CodeInsightSettings.ASK
        if (!shouldShowDialog || referencesToRestore.isEmpty()) {
            return referencesToRestore
        }

        val fqNames = referencesToRestore.map { it.refData.fqName }.toSortedSet()

        val dialog = RestoreReferencesDialog(project, fqNames.copyToArray())
        dialog.show()

        val selectedFqNames = dialog.getSelectedElements()!!.toSet()
        return referencesToRestore.filter { selectedFqNames.contains(it.refData.fqName) }
    }

    private fun toTextRanges(startOffsets: IntArray, endOffsets: IntArray): List<TextRange> {
        assert(startOffsets.size() == endOffsets.size())
        return startOffsets.indices.map { TextRange(startOffsets[it], endOffsets[it]) }
    }

    private fun PsiElement.isInCopiedArea(fileCopiedFrom: JetFile, startOffsets: IntArray, endOffsets: IntArray): Boolean {
        if (getContainingFile() != fileCopiedFrom) return false
        return toTextRanges(startOffsets, endOffsets).any { this.range in it }
    }
}

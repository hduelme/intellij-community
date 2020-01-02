/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.nj2k;

import com.intellij.testFramework.TestDataPath;
import org.jetbrains.kotlin.test.JUnit3RunnerWithInners;
import org.jetbrains.kotlin.test.KotlinTestUtils;
import org.jetbrains.kotlin.test.TestMetadata;
import org.junit.runner.RunWith;

import java.io.File;
import java.util.regex.Pattern;

/** This class is generated by {@link org.jetbrains.kotlin.generators.tests.TestsPackage}. DO NOT MODIFY MANUALLY */
@SuppressWarnings("all")
@TestMetadata("nj2k/testData/copyPaste")
@TestDataPath("$PROJECT_ROOT")
@RunWith(JUnit3RunnerWithInners.class)
public class NewJavaToKotlinCopyPasteConversionTestGenerated extends AbstractNewJavaToKotlinCopyPasteConversionTest {
    private void runTest(String testDataFilePath) throws Exception {
        KotlinTestUtils.runTest(this::doTest, this, testDataFilePath);
    }

    @TestMetadata("AddImports.java")
    public void testAddImports() throws Exception {
        runTest("nj2k/testData/copyPaste/AddImports.java");
    }

    @TestMetadata("AddImportsButNoConversion.java")
    public void testAddImportsButNoConversion() throws Exception {
        runTest("nj2k/testData/copyPaste/AddImportsButNoConversion.java");
    }

    @TestMetadata("AddImportsClassInSamePackage.java")
    public void testAddImportsClassInSamePackage() throws Exception {
        runTest("nj2k/testData/copyPaste/AddImportsClassInSamePackage.java");
    }

    @TestMetadata("AddImportsDummyConflict.java")
    public void testAddImportsDummyConflict() throws Exception {
        runTest("nj2k/testData/copyPaste/AddImportsDummyConflict.java");
    }

    @TestMetadata("AddImportsWithExplicitImports.java")
    public void testAddImportsWithExplicitImports() throws Exception {
        runTest("nj2k/testData/copyPaste/AddImportsWithExplicitImports.java");
    }

    @TestMetadata("AddKClassImport.java")
    public void testAddKClassImport() throws Exception {
        runTest("nj2k/testData/copyPaste/AddKClassImport.java");
    }

    public void testAllFilesPresentInCopyPaste() throws Exception {
        KotlinTestUtils.assertAllTestsPresentByMetadataWithExcluded(this.getClass(), new File("nj2k/testData/copyPaste"), Pattern.compile("^([^\\.]+)\\.java$"), null, true);
    }

    @TestMetadata("Arithmetic.java")
    public void testArithmetic() throws Exception {
        runTest("nj2k/testData/copyPaste/Arithmetic.java");
    }

    @TestMetadata("ClassWithNoDocComment.java")
    public void testClassWithNoDocComment() throws Exception {
        runTest("nj2k/testData/copyPaste/ClassWithNoDocComment.java");
    }

    @TestMetadata("ClassWithOverrides.java")
    public void testClassWithOverrides() throws Exception {
        runTest("nj2k/testData/copyPaste/ClassWithOverrides.java");
    }

    @TestMetadata("Constructor.java")
    public void testConstructor() throws Exception {
        runTest("nj2k/testData/copyPaste/Constructor.java");
    }

    @TestMetadata("ConversionInCorrectContext.java")
    public void testConversionInCorrectContext() throws Exception {
        runTest("nj2k/testData/copyPaste/ConversionInCorrectContext.java");
    }

    @TestMetadata("CopyAnnotation.java")
    public void testCopyAnnotation() throws Exception {
        runTest("nj2k/testData/copyPaste/CopyAnnotation.java");
    }

    @TestMetadata("ExtendsTypeRef.java")
    public void testExtendsTypeRef() throws Exception {
        runTest("nj2k/testData/copyPaste/ExtendsTypeRef.java");
    }

    @TestMetadata("FieldWithNoEndComment.java")
    public void testFieldWithNoEndComment() throws Exception {
        runTest("nj2k/testData/copyPaste/FieldWithNoEndComment.java");
    }

    @TestMetadata("FieldWithNoModifierAndNoSemicolon.java")
    public void testFieldWithNoModifierAndNoSemicolon() throws Exception {
        runTest("nj2k/testData/copyPaste/FieldWithNoModifierAndNoSemicolon.java");
    }

    @TestMetadata("FileWithNoPackageStatement.java")
    public void testFileWithNoPackageStatement() throws Exception {
        runTest("nj2k/testData/copyPaste/FileWithNoPackageStatement.java");
    }

    @TestMetadata("HalfTheWhiteSpace.java")
    public void testHalfTheWhiteSpace() throws Exception {
        runTest("nj2k/testData/copyPaste/HalfTheWhiteSpace.java");
    }

    @TestMetadata("ImplementsTypeRef.java")
    public void testImplementsTypeRef() throws Exception {
        runTest("nj2k/testData/copyPaste/ImplementsTypeRef.java");
    }

    @TestMetadata("Imports1.java")
    public void testImports1() throws Exception {
        runTest("nj2k/testData/copyPaste/Imports1.java");
    }

    @TestMetadata("Imports2.java")
    public void testImports2() throws Exception {
        runTest("nj2k/testData/copyPaste/Imports2.java");
    }

    @TestMetadata("Imports3.java")
    public void testImports3() throws Exception {
        runTest("nj2k/testData/copyPaste/Imports3.java");
    }

    @TestMetadata("Indentation.java")
    public void testIndentation() throws Exception {
        runTest("nj2k/testData/copyPaste/Indentation.java");
    }

    @TestMetadata("InsertIntoComment.java")
    public void testInsertIntoComment() throws Exception {
        runTest("nj2k/testData/copyPaste/InsertIntoComment.java");
    }

    @TestMetadata("InsertIntoString.java")
    public void testInsertIntoString() throws Exception {
        runTest("nj2k/testData/copyPaste/InsertIntoString.java");
    }

    @TestMetadata("MethodDeclarationWithNoBody.java")
    public void testMethodDeclarationWithNoBody() throws Exception {
        runTest("nj2k/testData/copyPaste/MethodDeclarationWithNoBody.java");
    }

    @TestMetadata("MethodReferenceWithoutQualifier.java")
    public void testMethodReferenceWithoutQualifier() throws Exception {
        runTest("nj2k/testData/copyPaste/MethodReferenceWithoutQualifier.java");
    }

    @TestMetadata("MethodWithNoAnnotation.java")
    public void testMethodWithNoAnnotation() throws Exception {
        runTest("nj2k/testData/copyPaste/MethodWithNoAnnotation.java");
    }

    @TestMetadata("MethodWithOnlyOneAnnotation.java")
    public void testMethodWithOnlyOneAnnotation() throws Exception {
        runTest("nj2k/testData/copyPaste/MethodWithOnlyOneAnnotation.java");
    }

    @TestMetadata("OnlyClosingBrace.java")
    public void testOnlyClosingBrace() throws Exception {
        runTest("nj2k/testData/copyPaste/OnlyClosingBrace.java");
    }

    @TestMetadata("OnlyOneBraceFromBlock.java")
    public void testOnlyOneBraceFromBlock() throws Exception {
        runTest("nj2k/testData/copyPaste/OnlyOneBraceFromBlock.java");
    }

    @TestMetadata("OnlyQualifier.java")
    public void testOnlyQualifier() throws Exception {
        runTest("nj2k/testData/copyPaste/OnlyQualifier.java");
    }

    @TestMetadata("RawTypeRef.java")
    public void testRawTypeRef() throws Exception {
        runTest("nj2k/testData/copyPaste/RawTypeRef.java");
    }

    @TestMetadata("RedundantTypeCast.java")
    public void testRedundantTypeCast() throws Exception {
        runTest("nj2k/testData/copyPaste/RedundantTypeCast.java");
    }

    @TestMetadata("RedundantTypeCast2.java")
    public void testRedundantTypeCast2() throws Exception {
        runTest("nj2k/testData/copyPaste/RedundantTypeCast2.java");
    }

    @TestMetadata("SampleBlock.java")
    public void testSampleBlock() throws Exception {
        runTest("nj2k/testData/copyPaste/SampleBlock.java");
    }

    @TestMetadata("SeveralMethodsSample.java")
    public void testSeveralMethodsSample() throws Exception {
        runTest("nj2k/testData/copyPaste/SeveralMethodsSample.java");
    }

    @TestMetadata("SingleWordFromIdentifier.java")
    public void testSingleWordFromIdentifier() throws Exception {
        runTest("nj2k/testData/copyPaste/SingleWordFromIdentifier.java");
    }
}

/*
 * Copyright 2000-2008 JetBrains s.r.o.
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

package org.jetbrains.plugins.groovy.lang.groovydoc.psi.impl;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiType;
import com.intellij.psi.PsiElementFactory;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.impl.PsiElementFactoryImpl;
import com.intellij.psi.impl.PsiManagerEx;
import com.intellij.lang.ASTNode;
import com.intellij.util.IncorrectOperationException;
import com.intellij.openapi.diagnostic.Logger;
import org.jetbrains.plugins.groovy.lang.groovydoc.psi.api.GrDocMethodParams;
import org.jetbrains.plugins.groovy.lang.groovydoc.psi.api.GrDocMethodParameter;
import org.jetbrains.plugins.groovy.lang.groovydoc.psi.api.GrDocReferenceElement;
import org.jetbrains.plugins.groovy.lang.psi.GroovyElementVisitor;
import org.jetbrains.plugins.groovy.lang.psi.impl.statements.expressions.TypesUtil;
import org.jetbrains.plugins.groovy.lang.psi.api.types.GrCodeReferenceElement;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.typedef.GrTypeDefinition;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

/**
 * @author ilyas
 */
public class GrDocMethodParamsImpl extends GroovyDocPsiElementImpl implements GrDocMethodParams {

  private static final Logger LOG = Logger.getInstance("#org.jetbrains.plugins.groovy.lang.groovydoc.psi.impl.GrDocMethodParamsImpl");

  public GrDocMethodParamsImpl(@NotNull ASTNode node) {
    super(node);
  }

  public String toString() {
    return "GrDocMethodParameterList";
  }

  public void accept(GroovyElementVisitor visitor) {
    visitor.visitDocMethodParameterList(this);
  }

  public PsiType[] getParameterTypes() {
    ArrayList<PsiType> types = new ArrayList<PsiType>();
    PsiManagerEx manager = getManager();
    GlobalSearchScope scope = GlobalSearchScope.allScope(getProject());
    PsiElementFactory factory = manager.getElementFactory();
    for (GrDocMethodParameter parameter : getParameters()) {
      GrDocReferenceElement typeElement = parameter.getTypeElement();
      GrCodeReferenceElement referenceElement = typeElement.getReferenceElement();
      try {
        PsiType type = factory.createTypeFromText(referenceElement.getText(), this);
        type = TypesUtil.boxPrimitiveType(type, manager, scope);
        types.add(type);
      } catch (IncorrectOperationException e) {
        LOG.info(e);
        types.add(null);
      }
    }
    return types.toArray(new PsiType[types.size()]);
  }

  public GrDocMethodParameter[] getParameters() {
    return findChildrenByClass(GrDocMethodParameter.class);
  }
}

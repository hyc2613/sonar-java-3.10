/*
 * SonarQube Java
 * Copyright (C) 2012-2016 SonarSource SA
 * mailto:contact AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonar.java.checks;

import org.sonar.api.server.rule.RulesDefinition;
import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;
import org.sonar.java.model.declaration.MethodTreeImpl;
import org.sonar.java.tag.Tag;
import org.sonar.plugins.java.api.JavaFileScanner;
import org.sonar.plugins.java.api.JavaFileScannerContext;
import org.sonar.plugins.java.api.tree.BaseTreeVisitor;
import org.sonar.plugins.java.api.tree.MethodTree;
import org.sonar.plugins.java.api.tree.Tree;
import org.sonar.squidbridge.annotations.ActivatedByDefault;
import org.sonar.squidbridge.annotations.SqaleConstantRemediation;
import org.sonar.squidbridge.annotations.SqaleSubCharacteristic;

@Rule(
  key = "S00107",
  name = "Methods should not have too many parameters",
  priority = Priority.MAJOR,
  tags = {Tag.BRAIN_OVERLOAD})
@ActivatedByDefault
@SqaleSubCharacteristic(RulesDefinition.SubCharacteristics.UNIT_TESTABILITY)
@SqaleConstantRemediation("20min")
public class TooManyParametersCheck extends BaseTreeVisitor implements JavaFileScanner {

  private static final int DEFAULT_MAXIMUM = 7;

  @RuleProperty(
    key = "max",
    description = "Maximum authorized number of parameters",
    defaultValue = "" + DEFAULT_MAXIMUM)
  public int maximum = DEFAULT_MAXIMUM;

  @RuleProperty(
    key = "constructorMax",
    description = "Maximum authorized number of parameters for a constructor",
    defaultValue = "" + DEFAULT_MAXIMUM)
  public int constructorMax = DEFAULT_MAXIMUM;

  private JavaFileScannerContext context;

  @Override
  public void scanFile(JavaFileScannerContext context) {
    this.context = context;
    scan(context.getTree());
  }

  @Override
  public void visitMethod(MethodTree tree) {
    super.visitMethod(tree);
    if (Boolean.TRUE.equals(((MethodTreeImpl) tree).isOverriding())) {
      return;
    }
    int max;
    String partialMessage;
    if (tree.is(Tree.Kind.CONSTRUCTOR)) {
      max = constructorMax;
      partialMessage = "Constructor";
    } else {
      max = maximum;
      partialMessage = "Method";
    }
    int size = tree.parameters().size();
    if (size > max) {
      context.reportIssue(this, tree.simpleName(), partialMessage + " has " + size + " parameters, which is greater than " + max + " authorized.");
    }
  }

}

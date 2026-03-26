package org.jetbrains.research.refactorinsight.processors;

import org.jetbrains.research.refactorinsight.data.JavaRefactoringHandler;
import org.jetbrains.research.refactorinsight.data.attributes.AddAttributeAnnotationJavaHandler;
import org.jetbrains.research.refactorinsight.data.attributes.AddAttributeModifierJavaHandler;
import org.jetbrains.research.refactorinsight.data.attributes.ChangeAttributeAccessModifierJavaHandler;
import org.jetbrains.research.refactorinsight.data.attributes.ChangeAttributeTypeJavaHandler;
import org.jetbrains.research.refactorinsight.data.attributes.EncapsulateAttributeJavaHandler;
import org.jetbrains.research.refactorinsight.data.attributes.ExtractAttributeJavaHandler;
import org.jetbrains.research.refactorinsight.data.attributes.InlineAttributeJavaHandler;
import org.jetbrains.research.refactorinsight.data.attributes.MergeAttributeJavaHandler;
import org.jetbrains.research.refactorinsight.data.attributes.ModifyAttributeAnnotationJavaHandler;
import org.jetbrains.research.refactorinsight.data.attributes.MoveAttributeJavaHandler;
import org.jetbrains.research.refactorinsight.data.attributes.MoveRenameAttributeJavaHandler;
import org.jetbrains.research.refactorinsight.data.attributes.PullUpAttributeJavaHandler;
import org.jetbrains.research.refactorinsight.data.attributes.PushDownAttributeJavaHandler;
import org.jetbrains.research.refactorinsight.data.attributes.RemoveAttributeAnnotationJavaHandler;
import org.jetbrains.research.refactorinsight.data.attributes.RemoveAttributeModifierJavaHandler;
import org.jetbrains.research.refactorinsight.data.attributes.RenameAttributeJavaHandler;
import org.jetbrains.research.refactorinsight.data.attributes.ReplaceAttributeJavaHandler;
import org.jetbrains.research.refactorinsight.data.attributes.SplitAttributeJavaHandler;
import org.jetbrains.research.refactorinsight.data.classes.AddClassAnnotationJavaHandler;
import org.jetbrains.research.refactorinsight.data.classes.AddClassModifierJavaHandler;
import org.jetbrains.research.refactorinsight.data.classes.ChangeClassAccessModifierJavaHandler;
import org.jetbrains.research.refactorinsight.data.classes.ChangeTypeDeclarationKindJavaHandler;
import org.jetbrains.research.refactorinsight.data.classes.CollapseHierarchyJavaHandler;
import org.jetbrains.research.refactorinsight.data.classes.ExtractClassJavaHandler;
import org.jetbrains.research.refactorinsight.data.classes.ExtractSuperClassJavaHandler;
import org.jetbrains.research.refactorinsight.data.classes.MergeClassJavaHandler;
import org.jetbrains.research.refactorinsight.data.classes.ModifyClassAnnotationJavaHandler;
import org.jetbrains.research.refactorinsight.data.classes.MoveClassJavaHandler;
import org.jetbrains.research.refactorinsight.data.classes.MoveRenameClassJavaHandler;
import org.jetbrains.research.refactorinsight.data.classes.RemoveClassAnnotationJavaHandler;
import org.jetbrains.research.refactorinsight.data.classes.RemoveClassModifierJavaHandler;
import org.jetbrains.research.refactorinsight.data.classes.RenameClassJavaHandler;
import org.jetbrains.research.refactorinsight.data.classes.SplitClassJavaHandler;
import org.jetbrains.research.refactorinsight.data.methods.AddMethodAnnotationJavaHandler;
import org.jetbrains.research.refactorinsight.data.methods.AddMethodModifierJavaHandler;
import org.jetbrains.research.refactorinsight.data.methods.AddParameterAnnotationJavaHandler;
import org.jetbrains.research.refactorinsight.data.methods.AddParameterJavaHandler;
import org.jetbrains.research.refactorinsight.data.methods.AddThrownExceptionTypeJavaHandler;
import org.jetbrains.research.refactorinsight.data.methods.ChangeMethodAccessModifierJavaHandler;
import org.jetbrains.research.refactorinsight.data.methods.ChangeReturnTypeJavaHandler;
import org.jetbrains.research.refactorinsight.data.methods.ChangeThrownExceptionTypeJavaHandler;
import org.jetbrains.research.refactorinsight.data.methods.ExtractOperationJavaHandler;
import org.jetbrains.research.refactorinsight.data.methods.InlineOperationJavaHandler;
import org.jetbrains.research.refactorinsight.data.methods.InvertConditionJavaHandler;
import org.jetbrains.research.refactorinsight.data.methods.MergeCatchJavaHandler;
import org.jetbrains.research.refactorinsight.data.methods.MergeConditionalJavaHandler;
import org.jetbrains.research.refactorinsight.data.methods.ModifyMethodAnnotationJavaHandler;
import org.jetbrains.research.refactorinsight.data.methods.ModifyParameterAnnotationJavaHandler;
import org.jetbrains.research.refactorinsight.data.methods.MoveOperationJavaHandler;
import org.jetbrains.research.refactorinsight.data.methods.PullUpOperationJavaHandler;
import org.jetbrains.research.refactorinsight.data.methods.PushDownOperationJavaHandler;
import org.jetbrains.research.refactorinsight.data.methods.RemoveMethodAnnotationJavaHandler;
import org.jetbrains.research.refactorinsight.data.methods.RemoveMethodModifierJavaHandler;
import org.jetbrains.research.refactorinsight.data.methods.RemoveParameterAnnotationJavaHandler;
import org.jetbrains.research.refactorinsight.data.methods.RemoveParameterJavaHandler;
import org.jetbrains.research.refactorinsight.data.methods.RemoveThrownExceptionTypeJavaHandler;
import org.jetbrains.research.refactorinsight.data.methods.RenameMethodJavaHandler;
import org.jetbrains.research.refactorinsight.data.methods.ReorderParameterJavaHandler;
import org.jetbrains.research.refactorinsight.data.methods.ReplaceAnonymousWithLambdaJavaHandler;
import org.jetbrains.research.refactorinsight.data.methods.ReplaceLoopWithPipelineJavaHandler;
import org.jetbrains.research.refactorinsight.data.methods.ReplacePipelineWithLoopJavaHandler;
import org.jetbrains.research.refactorinsight.data.methods.SplitConditionalJavaHandler;
import org.jetbrains.research.refactorinsight.data.methods.SplitMethodJavaHandler;
import org.jetbrains.research.refactorinsight.data.methods.MergeMethodJavaHandler;
import org.jetbrains.research.refactorinsight.data.packages.MergePackageJavaHandler;
import org.jetbrains.research.refactorinsight.data.packages.MoveSourceFolderJavaHandler;
import org.jetbrains.research.refactorinsight.data.packages.RenamePackageJavaHandler;
import org.jetbrains.research.refactorinsight.data.packages.SplitPackageJavaHandler;
import org.jetbrains.research.refactorinsight.data.variables.AddVariableModifierJavaHandler;
import org.jetbrains.research.refactorinsight.data.variables.ChangeVariableTypeJavaHandler;
import org.jetbrains.research.refactorinsight.data.variables.ExtractVariableJavaHandler;
import org.jetbrains.research.refactorinsight.data.variables.InlineVariableJavaHandler;
import org.jetbrains.research.refactorinsight.data.variables.MergeVariableJavaHandler;
import org.jetbrains.research.refactorinsight.data.variables.RemoveVariableModifierJavaHandler;
import org.jetbrains.research.refactorinsight.data.variables.RenameVariableJavaHandler;
import org.jetbrains.research.refactorinsight.data.variables.SplitVariableJavaHandler;

import static org.jetbrains.research.refactorinsight.utils.StringUtils.getPrettyName;

public enum RefactoringType {
    EXTRACT_OPERATION("Extract Method", new ExtractOperationJavaHandler()),
    RENAME_CLASS("Rename Class", new RenameClassJavaHandler()),
    MOVE_ATTRIBUTE("Move Attribute", new MoveAttributeJavaHandler()),
    MOVE_RENAME_ATTRIBUTE("Move And Rename Attribute", new MoveRenameAttributeJavaHandler()),
    REPLACE_ATTRIBUTE("Replace Attribute", new ReplaceAttributeJavaHandler()),
    RENAME_METHOD("Rename Method", new RenameMethodJavaHandler()),
    INLINE_OPERATION("Inline Method", new InlineOperationJavaHandler()),
    MOVE_OPERATION("Move Method", new MoveOperationJavaHandler()),
    MOVE_AND_RENAME_OPERATION("Move And Rename Method", new MoveOperationJavaHandler()),
    PULL_UP_OPERATION("Pull Up Method", new PullUpOperationJavaHandler()),
    MOVE_CLASS("Move Class", new MoveClassJavaHandler()),
    MOVE_RENAME_CLASS("Move And Rename Class", new MoveRenameClassJavaHandler()),
    MOVE_SOURCE_FOLDER("Move Source Folder", new MoveSourceFolderJavaHandler()),
    PULL_UP_ATTRIBUTE("Pull Up Attribute", new PullUpAttributeJavaHandler()),
    PUSH_DOWN_ATTRIBUTE("Push Down Attribute", new PushDownAttributeJavaHandler()),
    PUSH_DOWN_OPERATION("Push Down Method", new PushDownOperationJavaHandler()),
    EXTRACT_INTERFACE("Extract Interface", new ExtractSuperClassJavaHandler()),
    EXTRACT_SUPERCLASS("Extract Superclass", new ExtractSuperClassJavaHandler()),
    EXTRACT_SUBCLASS("Extract Subclass", new ExtractClassJavaHandler()),
    EXTRACT_CLASS("Extract Class", new ExtractClassJavaHandler()),
    MERGE_OPERATION("Merge Method", null),
    EXTRACT_AND_MOVE_OPERATION("Extract And Move Method", new ExtractOperationJavaHandler()),
    MOVE_AND_INLINE_OPERATION("Move And Inline Method", new InlineOperationJavaHandler()),
    RENAME_PACKAGE("Change Package", new RenamePackageJavaHandler()),
    EXTRACT_VARIABLE("Extract Variable", new ExtractVariableJavaHandler()),
    EXTRACT_ATTRIBUTE("Extract Attribute", new ExtractAttributeJavaHandler()),
    INLINE_VARIABLE("Inline Variable", new InlineVariableJavaHandler()),
    RENAME_VARIABLE("Rename Variable", new RenameVariableJavaHandler()),
    RENAME_PARAMETER("Rename Parameter", new RenameVariableJavaHandler()),
    RENAME_ATTRIBUTE("Rename Attribute", new RenameAttributeJavaHandler()),
    MERGE_VARIABLE("Merge Variable", new MergeVariableJavaHandler()),
    MERGE_PARAMETER("Merge Parameter", new MergeVariableJavaHandler()),
    MERGE_ATTRIBUTE("Merge Attribute", new MergeAttributeJavaHandler()),
    SPLIT_VARIABLE("Split Variable", new SplitVariableJavaHandler()),
    SPLIT_PARAMETER("Split Parameter", new SplitVariableJavaHandler()),
    SPLIT_ATTRIBUTE("Split Attribute", new SplitAttributeJavaHandler()),
    REPLACE_VARIABLE_WITH_ATTRIBUTE("Replace Variable With Attribute", new RenameVariableJavaHandler()),
    PARAMETERIZE_VARIABLE("Parameterize Variable", new RenameVariableJavaHandler()),
    CHANGE_RETURN_TYPE("Change Return Type", new ChangeReturnTypeJavaHandler()),
    CHANGE_VARIABLE_TYPE("Change Variable Type", new ChangeVariableTypeJavaHandler()),
    CHANGE_PARAMETER_TYPE("Change Parameter Type", new ChangeVariableTypeJavaHandler()),
    CHANGE_ATTRIBUTE_TYPE("Change Attribute Type", new ChangeAttributeTypeJavaHandler()),
    ADD_METHOD_ANNOTATION("Add Method Annotation", new AddMethodAnnotationJavaHandler()),
    REMOVE_METHOD_ANNOTATION("Remove Method Annotation", new RemoveMethodAnnotationJavaHandler()),
    MODIFY_METHOD_ANNOTATION("Modify Method Annotation", new ModifyMethodAnnotationJavaHandler()),
    ADD_ATTRIBUTE_ANNOTATION("Add Attribute Annotation", new AddAttributeAnnotationJavaHandler()),
    REMOVE_ATTRIBUTE_ANNOTATION("Remove Attribute Annotation", new RemoveAttributeAnnotationJavaHandler()),
    MODIFY_ATTRIBUTE_ANNOTATION("Modify Attribute Annotation", new ModifyAttributeAnnotationJavaHandler()),
    ADD_CLASS_ANNOTATION("Add Class Annotation", new AddClassAnnotationJavaHandler()),
    REMOVE_CLASS_ANNOTATION("Remove Class Annotation", new RemoveClassAnnotationJavaHandler()),
    MODIFY_CLASS_ANNOTATION("Modify Class Annotation", new ModifyClassAnnotationJavaHandler()),
    ADD_PARAMETER_ANNOTATION("Add Parameter Annotation", new AddParameterAnnotationJavaHandler()),
    REMOVE_PARAMETER_ANNOTATION("Remove Parameter Annotation", new RemoveParameterAnnotationJavaHandler()),
    MODIFY_PARAMETER_ANNOTATION("Modify Parameter Annotation", new ModifyParameterAnnotationJavaHandler()),
    ADD_PARAMETER("Add Parameter", new AddParameterJavaHandler()),
    REMOVE_PARAMETER("Remove Parameter", new RemoveParameterJavaHandler()),
    REORDER_PARAMETER("Reorder Parameter", new ReorderParameterJavaHandler()),
    RENAME_AND_CHANGE_ATTRIBUTE_TYPE("Rename and Change Attribute Type", null),
    RENAME_AND_CHANGE_PARAMETER_TYPE("Rename and Change Parameter Type", null),
    RENAME_AND_CHANGE_VARIABLE_TYPE("Rename and Change Variable Type", null),
    ADD_THROWN_EXCEPTION_TYPE("Add Thrown Exception Type", new AddThrownExceptionTypeJavaHandler()),
    REMOVE_THROWN_EXCEPTION_TYPE("Remove Thrown Exception Type", new RemoveThrownExceptionTypeJavaHandler()),
    CHANGE_THROWN_EXCEPTION_TYPE("Change Thrown Exception Type", new ChangeThrownExceptionTypeJavaHandler()),
    CHANGE_OPERATION_ACCESS_MODIFIER("Change Method Access Modifier", new ChangeMethodAccessModifierJavaHandler()),
    CHANGE_ATTRIBUTE_ACCESS_MODIFIER("Change Attribute Access Modifier", new ChangeAttributeAccessModifierJavaHandler()),
    ENCAPSULATE_ATTRIBUTE("Encapsulate Attribute", new EncapsulateAttributeJavaHandler()),
    PARAMETERIZE_ATTRIBUTE("Parameterize Attribute", new RenameVariableJavaHandler()),
    REPLACE_ATTRIBUTE_WITH_VARIABLE("Replace Attribute with Variable", new RenameVariableJavaHandler()),
    ADD_METHOD_MODIFIER("Add Method Modifier", new AddMethodModifierJavaHandler()),
    REMOVE_METHOD_MODIFIER("Remove Method Modifier", new RemoveMethodModifierJavaHandler()),
    ADD_ATTRIBUTE_MODIFIER("Add Attribute Modifier", new AddAttributeModifierJavaHandler()),
    REMOVE_ATTRIBUTE_MODIFIER("Remove Attribute Modifier", new RemoveAttributeModifierJavaHandler()),
    ADD_VARIABLE_MODIFIER("Add Variable Modifier", new AddVariableModifierJavaHandler()),
    REMOVE_VARIABLE_MODIFIER("Remove Variable Modifier", new RemoveVariableModifierJavaHandler()),
    ADD_PARAMETER_MODIFIER("Add Parameter Modifier", new AddVariableModifierJavaHandler()),
    REMOVE_PARAMETER_MODIFIER("Remove Parameter Modifier", new RemoveVariableModifierJavaHandler()),
    ADD_CLASS_MODIFIER("Add Class Modifier", new AddClassModifierJavaHandler()),
    REMOVE_CLASS_MODIFIER("Remove Class Modifier", new RemoveClassModifierJavaHandler()),
    CHANGE_CLASS_ACCESS_MODIFIER("Change Class Access Modifier", new ChangeClassAccessModifierJavaHandler()),
    MOVE_PACKAGE("Move Package", new RenamePackageJavaHandler()),
    SPLIT_PACKAGE("Split Package", new SplitPackageJavaHandler()),
    MERGE_PACKAGE("Merge Package", new MergePackageJavaHandler()),
    LOCALIZE_PARAMETER("Localize Parameter", new RenameVariableJavaHandler()),
    CHANGE_TYPE_DECLARATION_KIND("Change Type Declaration Kind", new ChangeTypeDeclarationKindJavaHandler()),
    COLLAPSE_HIERARCHY("Collapse Hierarchy", new CollapseHierarchyJavaHandler()),
    REPLACE_LOOP_WITH_PIPELINE("Replace Loop with Pipeline", new ReplaceLoopWithPipelineJavaHandler()),
    REPLACE_ANONYMOUS_WITH_LAMBDA("Replace Anonymous with Lambda", new ReplaceAnonymousWithLambdaJavaHandler()),
    MERGE_CLASS("Merge Class", new MergeClassJavaHandler()),
    INLINE_ATTRIBUTE("Inline Attribute", new InlineAttributeJavaHandler()),
    REPLACE_PIPELINE_WITH_LOOP("Replace Pipeline with Loop", new ReplacePipelineWithLoopJavaHandler()),
    SPLIT_CLASS("Split Class", new SplitClassJavaHandler()),
    SPLIT_CONDITIONAL("Split Conditional", new SplitConditionalJavaHandler()),
    INVERT_CONDITIONAL("Invert Condition", new InvertConditionJavaHandler()),
    MERGE_CONDITIONAL("Merge Conditional", new MergeConditionalJavaHandler()),
    MERGE_CATCH("Merge Catch", new MergeCatchJavaHandler()),
    MERGE_METHOD("Merge Method", new MergeMethodJavaHandler()),
    SPLIT_METHOD("Split Method", new SplitMethodJavaHandler());

    private final String name;
    private final String prettyName;
    private final JavaRefactoringHandler javaHandler;

    RefactoringType(String name, JavaRefactoringHandler javaHandler) {
        this.name = name;
        this.prettyName = getPrettyName(name);
        this.javaHandler = javaHandler;
    }

    public String getName() {
        return prettyName;
    }

    public JavaRefactoringHandler getJavaHandler() {
        return this.javaHandler;
    }

    @Override
    public String toString() {
        return prettyName;
    }
}

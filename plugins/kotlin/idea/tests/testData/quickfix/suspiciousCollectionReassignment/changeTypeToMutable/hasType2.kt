// "Change type to mutable" "true"
// TOOL: org.jetbrains.kotlin.idea.inspections.SuspiciousCollectionReassignmentInspection
// WITH_STDLIB
fun test() {
    var list = listOf<Int>(1)
    list +=<caret> 2
}
// FUS_QUICKFIX_NAME: org.jetbrains.kotlin.idea.inspections.SuspiciousCollectionReassignmentInspection$ChangeTypeToMutableFix
package preprocessors

final class CloudformationPreprocessor extends OperationFilteringPreprocessor {
  override lazy val shapesToKeep: Set[String] = Set(
    "DescribeStackResources",
  )

  override lazy val namespace: Set[String] = Set("com.amazonaws.cloudformation")

  override def getName: String = "CloudformationPreprocessor"
}

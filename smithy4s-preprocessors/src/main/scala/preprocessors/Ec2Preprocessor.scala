package preprocessors

final class Ec2Preprocessor extends OperationFilteringPreprocessor {
  override lazy val shapesToKeep: Set[String] = Set(
    "DescribeInstances",
  )

  override lazy val namespace: Set[String] = Set("com.amazonaws.ec2")

  override def getName: String = "Ec2Preprocessor"
}

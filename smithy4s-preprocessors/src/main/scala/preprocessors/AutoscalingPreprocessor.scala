package preprocessors

final class AutoscalingPreprocessor extends OperationFilteringPreprocessor {
  override lazy val shapesToKeep: Set[String] = Set(
    "CompleteLifecycleAction",
    "DescribeAutoScalingInstances",
    "LifecycleTransition",
    "LifecycleState",
  )

  override lazy val namespace: Set[String] = Set("com.amazonaws.autoscaling")

  override def getName: String = "AutoscalingPreprocessor"
}

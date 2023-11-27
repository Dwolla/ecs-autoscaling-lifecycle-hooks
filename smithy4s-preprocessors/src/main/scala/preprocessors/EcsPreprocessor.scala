package preprocessors

final class EcsPreprocessor extends OperationFilteringPreprocessor {
  override lazy val shapesToKeep: Set[String] = Set(
    "DescribeContainerInstances",
    "DescribeTasks",
    "ListClusters",
    "ListContainerInstances",
    "ListTasks",
    "UpdateContainerInstancesState",
  )

  override lazy val namespace: Set[String] = Set("com.amazonaws.ecs")

  override def getName: String = "EcsPreprocessor"
}

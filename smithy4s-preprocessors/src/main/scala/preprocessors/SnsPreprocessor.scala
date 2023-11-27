package preprocessors

final class SnsPreprocessor extends OperationFilteringPreprocessor {
  override lazy val shapesToKeep: Set[String] = Set(
    "Publish"
  )

  override lazy val namespace: Set[String] = Set("com.amazonaws.sns")

  override def getName: String = "SnsPreprocessor"
}

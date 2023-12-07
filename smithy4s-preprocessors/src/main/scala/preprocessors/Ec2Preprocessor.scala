package preprocessors
import software.amazon.smithy.build.TransformContext
import software.amazon.smithy.model.Model
import software.amazon.smithy.model.shapes.ShapeId

final class Ec2Preprocessor extends OperationFilteringPreprocessor {
  override lazy val shapesToKeep: Set[String] = Set(
    "DescribeInstances",
  )

  override lazy val namespace: Set[String] = Set("com.amazonaws.ec2")

  override def getName: String = "Ec2Preprocessor"

  override def transform(ctx: TransformContext): Model =
    ctx.getTransformer.filterShapes(
      super.transform(ctx),

      /* This seems to be a bug in smithy4s; MaxResults and InstanceIds cannot both be set on
       * a DescribeInstancesRequest. However, MaxResults is always set on the request, even if
       * it's set to the default value. This filter removes the MaxResults attribute from the
       * request class so that it can't ever be included.
       *
       * See https://github.com/disneystreaming/smithy4s/issues/1321
       */
      !_.toShapeId.equals(ShapeId.from("com.amazonaws.ec2#DescribeInstancesRequest$MaxResults"))
    )
}

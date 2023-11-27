package preprocessors

import software.amazon.smithy.build._
import software.amazon.smithy.model._
import software.amazon.smithy.model.neighbor.Walker
import software.amazon.smithy.model.shapes._

import scala.collection.JavaConverters._

abstract class OperationFilteringPreprocessor extends ProjectionTransformer {
  def shapesToKeep: Set[String]
  def namespace: Set[String]

  override def transform(ctx: TransformContext): Model = {
    val incomingModel = ctx.getModel

    val allShapesInNamespace = incomingModel.shapes()
      .toList
      .asScala
      .toSet
      .filter(shape => namespace.contains(shape.getId.getNamespace))

    val shapesNotExplicitlyKept: Set[Shape] = allShapesInNamespace
      .filterNot(shape => shapesToKeep.contains(shape.getId.getName))

    val remainingShapes = allShapesInNamespace -- shapesNotExplicitlyKept
    val shapeWalker = new Walker(incomingModel)

    val referencedShapeIds: Set[ShapeId] = remainingShapes.foldLeft(Set.empty[ShapeId]) {
      _ ++ shapeWalker.walkShapeIds(_).asScala
    }

    if (referencedShapeIds.nonEmpty) {
      ctx.getTransformer.filterShapes(incomingModel, { (t: Shape) =>
        !namespace.contains(t.getId.getNamespace) || referencedShapeIds.contains(t.toShapeId) || t.isServiceShape
      })
    } else {
      incomingModel
    }
  }
}

package $package$.infrastructure.dynamodb

import com.dimafeng.testcontainers.DynaliteContainer

import $package$.config.Configuration.AWSConfig
import $package$.config.Configuration.DynamoDbConfig
import zio._
import com.amazonaws.auth.AWSCredentialsProvider
import com.amazonaws.client.builder.AwsClientBuilder
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.auth.AWSStaticCredentialsProvider
import com.amazonaws.client.builder.AwsClientBuilder.EndpointConfiguration
import org.testcontainers.dynamodb.{ DynaliteContainer => JavaDynaliteContainer }
import org.testcontainers.utility.DockerImageName
import com.dimafeng.testcontainers.SingleContainer

final class DynamoContainer(
    dockerImageName: String = DynaliteContainer.defaultDockerImageName
  ) extends DynaliteContainer(DockerImageName.parse(dockerImageName))

object DynamoContainer:

  def make(
      dockerImageName: String = DynaliteContainer.defaultDockerImageName
    ): ZIO[Scope, Throwable, DynamoContainer] =
    ZIO.acquireRelease {
      ZIO
        .attempt {
          val c = new DynamoContainer(dockerImageName)
          c.start()
          c
        }
    } { container =>
      ZIO.attempt(container.stop()).orDie
    }

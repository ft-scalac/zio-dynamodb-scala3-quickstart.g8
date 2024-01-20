package scalac.infrastructure

import zio._
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.regions.Region
import zio.ZLayer
import zio.aws.dynamodb.DynamoDb
import zio.dynamodb.DynamoDBExecutor
import scalac.config.Configuration.AWSConfig
import zio.aws.core.config.CommonAwsConfig
import scalac.config.Configuration.DynamoDbConfig
import zio.aws.core.config.AwsConfig
import java.net.URI
import zio.aws.netty.NettyHttpClient

object DataSourceBuilderLive:

  val awsConfigLayer: ZLayer[AWSConfig, Nothing, CommonAwsConfig] = ZLayer
    .service[AWSConfig]
    .flatMap { cfg =>
      ZLayer.succeed(
        CommonAwsConfig(
          region = Some(Region.of(cfg.get.region)),
          credentialsProvider = StaticCredentialsProvider
            .create(AwsBasicCredentials.create(cfg.get.accessKeyId, cfg.get.secretAccessKey)),
          endpointOverride = None,
          commonClientConfig = None,
        )
      )
    }

  val dynamoDbLayer: ZLayer[AWSConfig & DynamoDbConfig & CommonAwsConfig, Throwable, DynamoDb] = for {
    awsConfig    <- ZLayer.service[AWSConfig]
    dynamoConfig <- ZLayer.service[DynamoDbConfig]
    layer        <-
      NettyHttpClient.default
        >>> AwsConfig.configured()
        >>> DynamoDb.customized { builder =>
          builder
            .endpointOverride(
              URI.create(s"http://${dynamoConfig.get.host}:${dynamoConfig.get.port}")
            )
        }
  } yield layer

  val dynamoDbExecutorLayer: ZLayer[DynamoDb, Throwable, DynamoDBExecutor] =
    DynamoDBExecutor.live

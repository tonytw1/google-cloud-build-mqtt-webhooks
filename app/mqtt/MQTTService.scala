package mqtt

import org.fusesource.mqtt.client.{MQTT, QoS}
import play.api.{Configuration, Logger}

import java.io.{File, FileInputStream}
import java.security.KeyStore
import java.security.cert.{CertificateFactory, X509Certificate}
import javax.inject.Inject
import javax.net.ssl.{SSLContext, TrustManagerFactory}

class MQTTService @Inject()(configuration: Configuration) {

  private val host = configuration.getString("mqtt.host").get
  private val port = configuration.getInt("mqtt.port").get
  private val topic = configuration.getString("mqtt.topic").get
  private val caCert = configuration.getString("mqtt.tls.cacert")

  private val statusesTopic = Seq(topic, "statuses").mkString("/")

  def publish(message: String, status: String) = {
    Logger.info("Publishing to mqtt topic " + host + ":" + port + " / " + topic + ": " + message)
    val connection = getClient().blockingConnection
    connection.connect
    connection.publish(topic, message.getBytes, QoS.AT_MOST_ONCE, false)
    connection.publish(statusesTopic, status.getBytes, QoS.AT_MOST_ONCE, false)
    Logger.info("Published")
    connection.disconnect
  }

  private def getClient(): MQTT = {

    def sslContext(certPath: String): SSLContext = {
      val cf = CertificateFactory.getInstance("X.509")
      val asStream = new FileInputStream(new File(certPath))
      val caCert = cf.generateCertificate(asStream).asInstanceOf[X509Certificate]
      val tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm)
      val ks = KeyStore.getInstance(KeyStore.getDefaultType)
      ks.load(null)
      ks.setCertificateEntry("caCert", caCert)
      tmf.init(ks)
      val sslContext = SSLContext.getInstance("TLS")
      sslContext.init(null, tmf.getTrustManagers, null)
      sslContext
    }

    val nonEmptyCaCert = caCert.flatMap { ca =>
      if (ca.trim.nonEmpty) {
        Some(ca.trim)
      } else {
        None
      }
    }

    nonEmptyCaCert.fold {
      val mqtt = new MQTT()
      mqtt.setHost(host, port)
      mqtt
    } { c =>
      val mqtt = new MQTT()
      mqtt.setHost("tls://" + host + ":" + port)
      mqtt.setSslContext(sslContext(c))
      mqtt
    }
  }

}
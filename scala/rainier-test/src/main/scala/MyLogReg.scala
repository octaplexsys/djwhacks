/*
MyLogReg.scala

Try doing a logistic regression model using Rainier

 */

import com.stripe.rainier.compute._
import com.stripe.rainier.core._
import com.stripe.rainier.sampler._
import com.stripe.rainier.repl._

case class Bernoulli(p: Real) extends Distribution[Int] {

  def logDensity(b: Int): Real = {
    p.log * b + (Real.one - p).log * (1 - b)
  }

  val generator = Generator.from { (r, n) =>
    val pd = n.toDouble(p)
    val u = r.standardUniform
    if (u < pd) 1 else 0
  }

}

object MyLogReg {

  def main(args: Array[String]): Unit = {

    // first simulate some data from a logistic regression model
    val r = new scala.util.Random
    val N = 1000
    val beta0 = 0.1
    val beta1 = 0.3
    val x = (1 to N) map { i =>
      3.0 * r.nextGaussian
    }
    val theta = x map { xi =>
      beta0 + beta1 * xi
    }
    def expit(x: Double): Double = 1.0 / (1.0 + math.exp(-x))
    val p = theta map expit
    val y = p map (pi => if (r.nextDouble < pi) 1 else 0)

    // now build and fit model
    val model = for {
      beta0 <- Normal(0, 5).param
      beta1 <- Normal(0, 5).param
      _ <- Predictor
        .from { x: Double =>
          {
            val theta = beta0 + beta1 * x
            val p = Real(1.0) / (Real(1.0) + (Real(0.0) - theta).exp)
            //Bernoulli(p)
            Binomial(p,1)
          }
        }
        .fit(x zip y)
    } yield (beta0, beta1)

    implicit val rng = RNG.default
    val its = 10000
    //val out = model.sample(Walkers(100), 10000, its)
    val out = model.sample(HMC(5), 10000, its)
    //val out = model.toStream(HMC(5), 1000).take(its).map(_()).toArray
    println(out.take(10))

    /*
    println(s"b0 (true value $beta0):")
    println(DensityPlot().plot1D(out map (_._1)).mkString("\n"))
    println(s"b1 (true value $beta1):")
    println(DensityPlot().plot1D(out map (_._2)).mkString("\n"))
    println("b1 against b0:")
    println(DensityPlot().plot2D(out).mkString("\n"))
     */

    import breeze.plot._
    import breeze.linalg._
    val fig = Figure("MCMC diagnostics")
    val p0 = fig.subplot(3,2,0)
    p0 += plot(linspace(1,its,its),out map (_._1))
    p0.title = s"b0 (true value $beta0):"
    p0.ylim = (-1,1)
    val p1 = fig.subplot(3,2,1)
    p1 += hist(out map (_._1))
    p1 += plot(linspace(beta0,beta0,2),linspace(0,p1.ylim._2,2))
    val p2 = fig.subplot(3,2,2)
    p2 += plot(linspace(1,its,its),out map (_._2))
    p2.title = s"b1 (true value $beta1):"
    p2.ylim = (-1,1)
    val p3 = fig.subplot(3,2,3)
    p3 += hist(out map (_._2))
    p3 += plot(linspace(beta1,beta1,2),linspace(0,p3.ylim._2,2))
    val p4 = fig.subplot(3,2,4)
    p4 += plot(out map (_._1),out map (_._2),'.')
    p4.xlabel = "b0"
    p4.ylabel = "b1"
    p4.title = "b1 against b0"
    p4.xlim = (-1,1)
    p4.ylim = (-1,1)
    p4 += plot(linspace(beta0,beta0,2),linspace(-1,1,2))
    p4 += plot(linspace(-1,1,2),linspace(beta1,beta1,2))

  }

}

// eof

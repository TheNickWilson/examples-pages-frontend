/*
 * Copyright 2018 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package connectors

import generators.Generators
import org.mockito.Mockito._
import org.mockito.Matchers.{eq => eqTo, _}
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mockito.MockitoSugar
import org.scalatest.prop.PropertyChecks
import org.scalatest.{MustMatchers, OptionValues, WordSpec}
import play.api.libs.json.{JsBoolean, JsNumber, JsString}
import repositories.{ReactiveMongoRepository, SessionRepository}
import uk.gov.hmrc.http.cache.client.CacheMap

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class MongoCacheConnectorSpec
  extends WordSpec with MustMatchers with PropertyChecks with Generators with MockitoSugar with ScalaFutures with OptionValues {

  ".save" must {

    "save the cache map to the Mongo repository" in {

      val mockReactiveMongoRepository = mock[ReactiveMongoRepository]
      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.apply()) thenReturn mockReactiveMongoRepository
      when(mockReactiveMongoRepository.upsert(any[CacheMap])) thenReturn Future(true)

      val mongoCacheConnector = new MongoCacheConnector(mockSessionRepository)

      val gen = for {
        cacheId <- nonEmptyString
        otherData <- Gen.mapOf(
          for {
            key <- Gen.alphaNumStr
            value <- Gen.oneOf(
              nonEmptyString.map(JsString),
              Gen.choose(0, Int.MaxValue).map(JsNumber(_)),
              arbitrary[Boolean].map(JsBoolean)
            )
          } yield (key, value)
        )
      } yield new CacheMap(cacheId, otherData)

      forAll(gen) {
        cacheMap =>

          val result = mongoCacheConnector.save(cacheMap)

          whenReady(result) {
            savedCacheMap =>

              savedCacheMap mustEqual cacheMap
              verify(mockReactiveMongoRepository).upsert(cacheMap)
          }
      }
    }
  }

  ".fetch" when {

    "there isn't a record for this key in Mongo" must {

      "return None" in {

        val mockReactiveMongoRepository = mock[ReactiveMongoRepository]
        val mockSessionRepository = mock[SessionRepository]

        when(mockSessionRepository.apply()) thenReturn mockReactiveMongoRepository
        when(mockReactiveMongoRepository.get(any())) thenReturn Future(None)

        val mongoCacheConnector = new MongoCacheConnector(mockSessionRepository)

        forAll(nonEmptyString) {
          cacheId =>

            val result = mongoCacheConnector.fetch(cacheId)

            whenReady(result) {
              optionalCacheMap =>

                optionalCacheMap must be(empty)
            }
        }
      }
    }

    "a record exists for this key" must {

      "return the record" in {

        val mockReactiveMongoRepository = mock[ReactiveMongoRepository]
        val mockSessionRepository = mock[SessionRepository]

        when(mockSessionRepository.apply()) thenReturn mockReactiveMongoRepository

        val mongoCacheConnector = new MongoCacheConnector(mockSessionRepository)

        val gen = for {
          cacheId <- nonEmptyString
          otherData <- Gen.mapOf(
            for {
              key <- Gen.alphaNumStr
              value <- Gen.oneOf(
                nonEmptyString.map(JsString),
                Gen.choose(0, Int.MaxValue).map(JsNumber(_)),
                arbitrary[Boolean].map(JsBoolean)
              )
            } yield (key, value)
          )
        } yield new CacheMap(cacheId, otherData)

        forAll(gen) {
          cacheMap =>

            when(mockReactiveMongoRepository.get(eqTo(cacheMap.id))) thenReturn Future(Some(cacheMap))

            val result = mongoCacheConnector.fetch(cacheMap.id)

            whenReady(result) {
              optionalCacheMap =>

                optionalCacheMap.value mustEqual cacheMap
            }
        }
      }
    }
  }

  ".getEntry" when {

    "there isn't a record for this key in Mongo" must {

      "return None" in {

        val mockReactiveMongoRepository = mock[ReactiveMongoRepository]
        val mockSessionRepository = mock[SessionRepository]

        when(mockSessionRepository.apply()) thenReturn mockReactiveMongoRepository
        when(mockReactiveMongoRepository.get(any())) thenReturn Future(None)

        val mongoCacheConnector = new MongoCacheConnector(mockSessionRepository)

        forAll(nonEmptyString, nonEmptyString) {
          (cacheId, key) =>

            val result = mongoCacheConnector.getEntry[String](cacheId, key)

            whenReady(result) {
              optionalValue =>

                optionalValue must be(empty)
            }
        }
      }
    }

    "a record exists in Mongo but this key is not present" must {

      "return None" in {

        val mockReactiveMongoRepository = mock[ReactiveMongoRepository]
        val mockSessionRepository = mock[SessionRepository]

        when(mockSessionRepository.apply()) thenReturn mockReactiveMongoRepository

        val mongoCacheConnector = new MongoCacheConnector(mockSessionRepository)

        val gen = for {
          cacheId   <- nonEmptyString
          key       <- nonEmptyString
          otherData <- Gen.mapOf(
            for {
              k <- Gen.alphaNumStr suchThat (_ != key)
              v <- Gen.oneOf(
                nonEmptyString.map(JsString),
                Gen.choose(0, Int.MaxValue).map(JsNumber(_)),
                arbitrary[Boolean].map(JsBoolean)
              )
            } yield (k, v)
          )
        } yield (key, new CacheMap(cacheId, otherData))

        forAll(gen) {
          case (key, cacheMap) =>

            when(mockReactiveMongoRepository.get(eqTo(cacheMap.id))) thenReturn Future(Some(cacheMap))

            val result = mongoCacheConnector.getEntry[String](cacheMap.id, key)

            whenReady(result) {
              optionalValue =>

                optionalValue must be(empty)
            }
        }
      }
    }

    "a record exists in Mongo with this key" must {

      "return the key's value" in {

        val mockReactiveMongoRepository = mock[ReactiveMongoRepository]
        val mockSessionRepository = mock[SessionRepository]

        when(mockSessionRepository.apply()) thenReturn mockReactiveMongoRepository

        val mongoCacheConnector = new MongoCacheConnector(mockSessionRepository)

        val gen = for {
          cacheId   <- nonEmptyString
          key       <- nonEmptyString
          value     <- nonEmptyString
          otherData <- Gen.mapOf(
            for {
              k <- Gen.alphaNumStr suchThat (_ != key)
              v <- Gen.oneOf(
                nonEmptyString.map(JsString),
                Gen.choose(0, Int.MaxValue).map(JsNumber(_)),
                arbitrary[Boolean].map(JsBoolean)
              )
            } yield (k, v)
          )
        } yield (key, value, new CacheMap(cacheId, otherData + (key -> JsString(value))))

        forAll(gen) {
          case (key, value, cacheMap) =>

            when(mockReactiveMongoRepository.get(eqTo(cacheMap.id))) thenReturn Future(Some(cacheMap))

            val result = mongoCacheConnector.getEntry[String](cacheMap.id, key)

            whenReady(result) {
              optionalValue =>

                optionalValue.value mustEqual value
            }
        }
      }
    }
  }
}

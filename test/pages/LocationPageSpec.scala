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

package pages

import controllers.routes
import generators.Generators
import org.scalacheck.Arbitrary.arbitrary
import models.{CheckMode, Location, NormalMode}
import models.Location._
import org.scalacheck.Gen
import pages.behaviours.PageBehaviours
import play.api.libs.json.Json
import uk.gov.hmrc.http.cache.client.CacheMap
import utils.UserAnswers

class YourLocationSpec extends PageBehaviours with Generators {

  "YourLocation" must {

    beRetrievable[Location](LocationPage)

    beSettable[Location](LocationPage)

    beRemovable[Location](LocationPage)
  }

  ".nextPage" when {

    "in Normal mode" must {

      "go to the ChildAgedTwo page" when {

        "the answer is not Northern Ireland" in {

          val gen = for {
            cacheMap <- arbitrary[CacheMap]
            original <- Gen.option(arbitrary[Location])
            answer   <- Gen.oneOf(England, Scotland, Wales)
          } yield (cacheMap copy (data = cacheMap.data + (LocationPage.toString -> Json.toJson(answer))), original)

          forAll(gen) {
            case (cacheMap, original) =>

              val result = LocationPage.nextPage(NormalMode, UserAnswers(cacheMap), original)
              result mustEqual routes.ChildAgedTwoController.onPageLoad(NormalMode)
          }
        }
      }

      "go to the ChildAgedThreeOrFour page" when {

        "the answer is Northern Ireland" in {

          val gen = for {
            cacheMap <- arbitrary[CacheMap]
            original <- Gen.option(arbitrary[Location])
          } yield (cacheMap copy (data = cacheMap.data + (LocationPage.toString -> Json.toJson(NorthernIreland))), original)

          forAll(gen) {
            case (cacheMap, original) =>

              val result = LocationPage.nextPage(NormalMode, UserAnswers(cacheMap), original)
              result mustEqual routes.ChildAgedThreeOrFourController.onPageLoad(NormalMode)
          }
        }
      }
    }

    "in Check mode" must {

      "go to the Check Your Answers page" when {

        "the answer is Northern Ireland" in {

          val gen = for {
            cacheMap <- arbitrary[CacheMap]
            original <- Gen.option(arbitrary[Location])
          } yield (cacheMap copy (data = cacheMap.data + (LocationPage.toString -> Json.toJson(NorthernIreland))), original)

          forAll(gen) {
            case (cacheMap, original) =>

              val result = LocationPage.nextPage(CheckMode, UserAnswers(cacheMap), original)
              result mustEqual routes.CheckYourAnswersController.onPageLoad()
          }
        }

        "the answer is not Northern Ireland and ChildAgedTwo is already answered" in {

          val gen = for {
            cacheMap     <- arbitrary[CacheMap]
            original     <- Gen.option(arbitrary[Location])
            answer       <- Gen.oneOf(England, Scotland, Wales)
            childAgedTwo <- arbitrary[Boolean].map(Some(_))
          } yield (
            cacheMap copy (data = cacheMap.data ++ Map(
              LocationPage.toString     -> Json.toJson(answer),
              ChildAgedTwoPage.toString -> Json.toJson(childAgedTwo)
            )), original)

          forAll(gen) {
            case (cacheMap, original) =>

              val result = LocationPage.nextPage(CheckMode, UserAnswers(cacheMap), original)
              result mustEqual routes.CheckYourAnswersController.onPageLoad()
          }
        }
      }

      "go to the ChildAgedTwo page" when {

        "the answer is not Northern Ireland and ChildAgedTwo hasn't been answered" in {

          val gen = for {
            cacheMap <- arbitrary[CacheMap]
            original <- Gen.option(arbitrary[Location])
            answer   <- Gen.oneOf(England, Scotland, Wales)
          } yield (cacheMap copy (data = cacheMap.data + (LocationPage.toString -> Json.toJson(answer)) - ChildAgedTwoPage.toString), original)

          forAll(gen) {
            case (cacheMap, original) =>

              val result = LocationPage.nextPage(CheckMode, UserAnswers(cacheMap), original)
              result mustEqual routes.ChildAgedTwoController.onPageLoad(CheckMode)
          }
        }
      }
    }
  }

  "Setting location" when {

    "the answer is Northern Ireland" must {

      "remove the answer for Child Aged Two" in {

        forAll(arbitrary[CacheMap]) {
          cacheMap =>

            val result = UserAnswers(cacheMap).set(LocationPage, NorthernIreland)

            result must equal(UserAnswers(cacheMap)) (after being strippedOf(ChildAgedTwoPage) and setTo(LocationPage, NorthernIreland))
        }
      }
    }
  }
}

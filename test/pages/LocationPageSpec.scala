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
import org.scalacheck.{Arbitrary, Gen}
import models.{CheckMode, NormalMode, Location}
import org.scalacheck.Gen
import pages.behaviours.PageBehaviours
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

      "go to the Index page" in {

        val gen = for {
          cacheMap <- arbitrary[CacheMap]
          original <- Gen.option(arbitrary[Location])
        } yield (cacheMap, original)

        forAll(gen) {
          case (cacheMap, original) =>

            val result = LocationPage.nextPage(NormalMode, UserAnswers(cacheMap), original)
            result mustEqual routes.IndexController.onPageLoad()
        }
      }
    }

    "in Check mode" must {

      "go to the Check Your Answers page" in {

        val gen = for {
          cacheMap <- arbitrary[CacheMap]
          original <- Gen.option(arbitrary[Location])
        } yield (cacheMap, original)

        forAll(gen) {
          case (cacheMap, original) =>

            val result = LocationPage.nextPage(CheckMode, UserAnswers(cacheMap), original)
            result mustEqual routes.CheckYourAnswersController.onPageLoad()
        }
      }
    }
  }
}

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

package generators

import models._
import org.scalacheck.Arbitrary
import org.scalacheck.Arbitrary.arbitrary
import pages._
import play.api.libs.json.{JsValue, Json}

trait UserAnswersEntryGenerators extends PageGenerators with ModelGenerators {

  implicit lazy val arbitraryYourDetailsUserAnswersEntry: Arbitrary[(YourDetailsPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[YourDetailsPage.type]
        value <- arbitrary[YourDetails].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryLocationUserAnswersEntry: Arbitrary[(LocationPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[LocationPage.type]
        value <- arbitrary[Location].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryChildAgedTwoUserAnswersEntry: Arbitrary[(ChildAgedTwoPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[ChildAgedTwoPage.type]
        value <- arbitrary[Boolean].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryChildAgedThreeOrFourUserAnswersEntry: Arbitrary[(ChildAgedThreeOrFourPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[ChildAgedThreeOrFourPage.type]
        value <- arbitrary[Boolean].map(Json.toJson(_))
      } yield (page, value)
    }
}

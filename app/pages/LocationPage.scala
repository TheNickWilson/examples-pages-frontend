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
import models.{CheckMode, Location, NormalMode}
import models.Location.NorthernIreland
import play.api.mvc.Call
import utils.UserAnswers

case object LocationPage extends QuestionPage[Location] {

  override def toString: String = "location"

  override def cleanup(value: Option[Location], userAnswers: UserAnswers): UserAnswers = value match {
    case Some(NorthernIreland) => userAnswers.remove(ChildAgedTwoPage)
    case _                     => userAnswers
  }

  override def normalModeRoute(answers: UserAnswers): Call =
    answers.get(LocationPage) match {
      case Some(NorthernIreland) => routes.ChildAgedThreeOrFourController.onPageLoad(NormalMode)
      case Some(_)               => routes.ChildAgedTwoController.onPageLoad(NormalMode)
      case None                  => routes.SessionExpiredController.onPageLoad()
    }

  override def checkModeRoute(answers: UserAnswers, data: Option[Location]): Call =
    (answers.get(LocationPage), answers.get(ChildAgedTwoPage)) match {
      case (Some(NorthernIreland), _) => routes.CheckYourAnswersController.onPageLoad()
      case (Some(_), Some(_))         => routes.CheckYourAnswersController.onPageLoad()
      case (Some(_), None)            => routes.ChildAgedTwoController.onPageLoad(CheckMode)
      case _                          => routes.SessionExpiredController.onPageLoad()
    }
}

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
import models.{CheckMode, Mode, NormalMode}
import play.api.mvc.Call
import utils.UserAnswers

trait QuestionPage[A] extends Page {

  def cleanup(value: Option[A], userAnswers: UserAnswers): UserAnswers = userAnswers

  def nextPage(mode: Mode, userAnswers: UserAnswers, original: Option[A]) = mode match {
    case NormalMode =>
      normalModeRoute(userAnswers)
    case CheckMode =>
      checkModeRoute(userAnswers, original)
  }

  protected def normalModeRoute(answers: UserAnswers): Call

  protected def checkModeRoute(answers: UserAnswers, data: Option[A]): Call
}

trait DefaultCheckModeRouting[A] {
  self: QuestionPage[A] =>

  override protected def checkModeRoute(answers: UserAnswers, data: Option[A]): Call =
    routes.CheckYourAnswersController.onPageLoad()
}

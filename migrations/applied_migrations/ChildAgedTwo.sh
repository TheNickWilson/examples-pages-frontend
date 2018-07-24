#!/bin/bash

echo "Applying migration ChildAgedTwo"

echo "Adding routes to conf/app.routes"

echo "" >> ../conf/app.routes
echo "GET        /childAgedTwo                        controllers.ChildAgedTwoController.onPageLoad(mode: Mode = NormalMode)" >> ../conf/app.routes
echo "POST       /childAgedTwo                        controllers.ChildAgedTwoController.onSubmit(mode: Mode = NormalMode)" >> ../conf/app.routes

echo "GET        /changeChildAgedTwo                  controllers.ChildAgedTwoController.onPageLoad(mode: Mode = CheckMode)" >> ../conf/app.routes
echo "POST       /changeChildAgedTwo                  controllers.ChildAgedTwoController.onSubmit(mode: Mode = CheckMode)" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "childAgedTwo.title = childAgedTwo" >> ../conf/messages.en
echo "childAgedTwo.heading = childAgedTwo" >> ../conf/messages.en
echo "childAgedTwo.checkYourAnswersLabel = childAgedTwo" >> ../conf/messages.en
echo "childAgedTwo.error.required = Select yes if childAgedTwo" >> ../conf/messages.en

echo "Adding to UserAnswersEntryGenerators"
awk '/trait UserAnswersEntryGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryChildAgedTwoUserAnswersEntry: Arbitrary[(ChildAgedTwoPage.type, JsValue)] =";\
    print "    Arbitrary {";\
    print "      for {";\
    print "        page  <- arbitrary[ChildAgedTwoPage.type]";\
    print "        value <- arbitrary[Boolean].map(Json.toJson(_))";\
    print "      } yield (page, value)";\
    print "    }";\
    next }1' ../test/generators/UserAnswersEntryGenerators.scala > tmp && mv tmp ../test/generators/UserAnswersEntryGenerators.scala

echo "Adding to PageGenerators"
awk '/trait PageGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryChildAgedTwoPage: Arbitrary[ChildAgedTwoPage.type] =";\
    print "    Arbitrary(ChildAgedTwoPage)";\
    next }1' ../test/generators/PageGenerators.scala > tmp && mv tmp ../test/generators/PageGenerators.scala

echo "Adding to CacheMapGenerator"
awk '/val generators/ {\
    print;\
    print "    arbitrary[(ChildAgedTwoPage.type, JsValue)] ::";\
    next }1' ../test/generators/CacheMapGenerator.scala > tmp && mv tmp ../test/generators/CacheMapGenerator.scala

echo "Adding helper method to CheckYourAnswersHelper"
awk '/class/ {\
     print;\
     print "";\
     print "  def childAgedTwo: Option[AnswerRow] = userAnswers.get(ChildAgedTwoPage) map {";\
     print "    x => AnswerRow(\"childAgedTwo.checkYourAnswersLabel\", if(x) \"site.yes\" else \"site.no\", true, routes.ChildAgedTwoController.onPageLoad(CheckMode).url)"; print "  }";\
     next }1' ../app/utils/CheckYourAnswersHelper.scala > tmp && mv tmp ../app/utils/CheckYourAnswersHelper.scala

echo "Migration ChildAgedTwo completed"

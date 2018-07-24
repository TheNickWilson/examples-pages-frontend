#!/bin/bash

echo "Applying migration ChildAgedThreeOrFour"

echo "Adding routes to conf/app.routes"

echo "" >> ../conf/app.routes
echo "GET        /childAgedThreeOrFour                        controllers.ChildAgedThreeOrFourController.onPageLoad(mode: Mode = NormalMode)" >> ../conf/app.routes
echo "POST       /childAgedThreeOrFour                        controllers.ChildAgedThreeOrFourController.onSubmit(mode: Mode = NormalMode)" >> ../conf/app.routes

echo "GET        /changeChildAgedThreeOrFour                  controllers.ChildAgedThreeOrFourController.onPageLoad(mode: Mode = CheckMode)" >> ../conf/app.routes
echo "POST       /changeChildAgedThreeOrFour                  controllers.ChildAgedThreeOrFourController.onSubmit(mode: Mode = CheckMode)" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "childAgedThreeOrFour.title = childAgedThreeOrFour" >> ../conf/messages.en
echo "childAgedThreeOrFour.heading = childAgedThreeOrFour" >> ../conf/messages.en
echo "childAgedThreeOrFour.checkYourAnswersLabel = childAgedThreeOrFour" >> ../conf/messages.en
echo "childAgedThreeOrFour.error.required = Select yes if childAgedThreeOrFour" >> ../conf/messages.en

echo "Adding to UserAnswersEntryGenerators"
awk '/trait UserAnswersEntryGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryChildAgedThreeOrFourUserAnswersEntry: Arbitrary[(ChildAgedThreeOrFourPage.type, JsValue)] =";\
    print "    Arbitrary {";\
    print "      for {";\
    print "        page  <- arbitrary[ChildAgedThreeOrFourPage.type]";\
    print "        value <- arbitrary[Boolean].map(Json.toJson(_))";\
    print "      } yield (page, value)";\
    print "    }";\
    next }1' ../test/generators/UserAnswersEntryGenerators.scala > tmp && mv tmp ../test/generators/UserAnswersEntryGenerators.scala

echo "Adding to PageGenerators"
awk '/trait PageGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryChildAgedThreeOrFourPage: Arbitrary[ChildAgedThreeOrFourPage.type] =";\
    print "    Arbitrary(ChildAgedThreeOrFourPage)";\
    next }1' ../test/generators/PageGenerators.scala > tmp && mv tmp ../test/generators/PageGenerators.scala

echo "Adding to CacheMapGenerator"
awk '/val generators/ {\
    print;\
    print "    arbitrary[(ChildAgedThreeOrFourPage.type, JsValue)] ::";\
    next }1' ../test/generators/CacheMapGenerator.scala > tmp && mv tmp ../test/generators/CacheMapGenerator.scala

echo "Adding helper method to CheckYourAnswersHelper"
awk '/class/ {\
     print;\
     print "";\
     print "  def childAgedThreeOrFour: Option[AnswerRow] = userAnswers.get(ChildAgedThreeOrFourPage) map {";\
     print "    x => AnswerRow(\"childAgedThreeOrFour.checkYourAnswersLabel\", if(x) \"site.yes\" else \"site.no\", true, routes.ChildAgedThreeOrFourController.onPageLoad(CheckMode).url)"; print "  }";\
     next }1' ../app/utils/CheckYourAnswersHelper.scala > tmp && mv tmp ../app/utils/CheckYourAnswersHelper.scala

echo "Migration ChildAgedThreeOrFour completed"

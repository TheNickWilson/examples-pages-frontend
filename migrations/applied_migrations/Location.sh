#!/bin/bash

echo "Applying migration Location"

echo "Adding routes to conf/app.routes"

echo "" >> ../conf/app.routes
echo "GET        /location               controllers.LocationController.onPageLoad(mode: Mode = NormalMode)" >> ../conf/app.routes
echo "POST       /location               controllers.LocationController.onSubmit(mode: Mode = NormalMode)" >> ../conf/app.routes

echo "GET        /changeLocation                  controllers.LocationController.onPageLoad(mode: Mode = CheckMode)" >> ../conf/app.routes
echo "POST       /changeLocation                  controllers.LocationController.onSubmit(mode: Mode = CheckMode)" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "location.title = Where do you live?" >> ../conf/messages.en
echo "location.heading = Where do you live?" >> ../conf/messages.en
echo "location.england = England" >> ../conf/messages.en
echo "location.scotland = Scotland" >> ../conf/messages.en
echo "location.checkYourAnswersLabel = Where do you live?" >> ../conf/messages.en
echo "location.error.required = Select location" >> ../conf/messages.en

echo "Adding to UserAnswersEntryGenerators"
awk '/trait UserAnswersEntryGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryLocationUserAnswersEntry: Arbitrary[(LocationPage.type, JsValue)] =";\
    print "    Arbitrary {";\
    print "      for {";\
    print "        page  <- arbitrary[LocationPage.type]";\
    print "        value <- arbitrary[Location].map(Json.toJson(_))";\
    print "      } yield (page, value)";\
    print "    }";\
    next }1' ../test/generators/UserAnswersEntryGenerators.scala > tmp && mv tmp ../test/generators/UserAnswersEntryGenerators.scala

echo "Adding to PageGenerators"
awk '/trait PageGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryLocationPage: Arbitrary[LocationPage.type] =";\
    print "    Arbitrary(LocationPage)";\
    next }1' ../test/generators/PageGenerators.scala > tmp && mv tmp ../test/generators/PageGenerators.scala

echo "Adding to ModelGenerators"
awk '/trait ModelGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryLocation: Arbitrary[Location] =";\
    print "    Arbitrary {";\
    print "      Gen.oneOf(Location.values.toSeq)";\
    print "    }";\
    next }1' ../test/generators/ModelGenerators.scala > tmp && mv tmp ../test/generators/ModelGenerators.scala

echo "Adding to CacheMapGenerator"
awk '/val generators/ {\
    print;\
    print "    arbitrary[(LocationPage.type, JsValue)] ::";\
    next }1' ../test/generators/CacheMapGenerator.scala > tmp && mv tmp ../test/generators/CacheMapGenerator.scala

echo "Adding helper method to CheckYourAnswersHelper"
awk '/class/ {\
     print;\
     print "";\
     print "  def location: Option[AnswerRow] = userAnswers.get(LocationPage) map {";\
     print "    x => AnswerRow(\"location.checkYourAnswersLabel\", s\"location.$x\", true, routes.LocationController.onPageLoad(CheckMode).url)";\
     print "  }";\
     next }1' ../app/utils/CheckYourAnswersHelper.scala > tmp && mv tmp ../app/utils/CheckYourAnswersHelper.scala

echo "Migration Location completed"

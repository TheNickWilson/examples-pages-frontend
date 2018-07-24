#!/bin/bash

echo "Applying migration YourDetails"

echo "Adding routes to conf/app.routes"

echo "" >> ../conf/app.routes
echo "GET        /yourDetails                        controllers.YourDetailsController.onPageLoad(mode: Mode = NormalMode)" >> ../conf/app.routes
echo "POST       /yourDetails                        controllers.YourDetailsController.onSubmit(mode: Mode = NormalMode)" >> ../conf/app.routes

echo "GET        /changeYourDetails                  controllers.YourDetailsController.onPageLoad(mode: Mode = CheckMode)" >> ../conf/app.routes
echo "POST       /changeYourDetails                  controllers.YourDetailsController.onSubmit(mode: Mode = CheckMode)" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "yourDetails.title = yourDetails" >> ../conf/messages.en
echo "yourDetails.heading = yourDetails" >> ../conf/messages.en
echo "yourDetails.field1 = Field 1" >> ../conf/messages.en
echo "yourDetails.field2 = Field 2" >> ../conf/messages.en
echo "yourDetails.checkYourAnswersLabel = yourDetails" >> ../conf/messages.en
echo "yourDetails.error.field1.required = Enter field1" >> ../conf/messages.en
echo "yourDetails.error.field2.required = Enter field2" >> ../conf/messages.en
echo "yourDetails.error.field1.length = field1 must be 100 characters or less" >> ../conf/messages.en
echo "yourDetails.error.field2.length = field2 must be 100 characters or less" >> ../conf/messages.en

echo "Adding to UserAnswersEntryGenerators"
awk '/trait UserAnswersEntryGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryYourDetailsUserAnswersEntry: Arbitrary[(YourDetailsPage.type, JsValue)] =";\
    print "    Arbitrary {";\
    print "      for {";\
    print "        page  <- arbitrary[YourDetailsPage.type]";\
    print "        value <- arbitrary[YourDetails].map(Json.toJson(_))";\
    print "      } yield (page, value)";\
    print "    }";\
    next }1' ../test/generators/UserAnswersEntryGenerators.scala > tmp && mv tmp ../test/generators/UserAnswersEntryGenerators.scala

echo "Adding to PageGenerators"
awk '/trait PageGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryYourDetailsPage: Arbitrary[YourDetailsPage.type] =";\
    print "    Arbitrary(YourDetailsPage)";\
    next }1' ../test/generators/PageGenerators.scala > tmp && mv tmp ../test/generators/PageGenerators.scala

echo "Adding to ModelGenerators"
awk '/trait ModelGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryYourDetails: Arbitrary[YourDetails] =";\
    print "    Arbitrary {";\
    print "      for {";\
    print "        field1 <- arbitrary[String]";\
    print "        field2 <- arbitrary[String]";\
    print "      } yield YourDetails(field1, field2)";\
    print "    }";\
    next }1' ../test/generators/ModelGenerators.scala > tmp && mv tmp ../test/generators/ModelGenerators.scala

echo "Adding to CacheMapGenerator"
awk '/val generators/ {\
    print;\
    print "    arbitrary[(YourDetailsPage.type, JsValue)] ::";\
    next }1' ../test/generators/CacheMapGenerator.scala > tmp && mv tmp ../test/generators/CacheMapGenerator.scala

echo "Adding helper method to CheckYourAnswersHelper"
awk '/class/ {\
     print;\
     print "";\
     print "  def yourDetails: Option[AnswerRow] = userAnswers.get(YourDetailsPage) map {";\
     print "    x => AnswerRow(\"yourDetails.checkYourAnswersLabel\", s\"${x.field1} ${x.field2}\", false, routes.YourDetailsController.onPageLoad(CheckMode).url)";\
     print "  }";\
     next }1' ../app/utils/CheckYourAnswersHelper.scala > tmp && mv tmp ../app/utils/CheckYourAnswersHelper.scala

echo "Migration YourDetails completed"

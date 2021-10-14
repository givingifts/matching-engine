.DEFAULT_GOAL := build
.PHONY: clean buildManager buildMatcher build

run:
	./out/manager/bin/matching-engine
clean:
	rm -rf out/
	./gradlew clean

buildManager:
	mkdir -p out/manager
	./gradlew installDist -PmainClass=gifts.givin.matching.manager.MainKt
	cp -R build/install/matching-engine/* out/manager/

buildMatcher:
	mkdir -p out/matcher
	./gradlew installDist -PmainClass=gifts.givin.matching.matcher.MainKt
	cp -R build/install/matching-engine/* out/matcher/

build: clean buildManager buildMatcher

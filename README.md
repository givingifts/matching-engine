<!-- shields -->
[![](https://img.shields.io/github/issues/givingifts/matching-engine)](https://github.com/givingifts/matching-engine/issues)
[![](https://img.shields.io/github/stars/givingifts/matching-engine)](https://github.com/givingifts/matching-engine/stargazers)
[![](https://img.shields.io/github/license/givingifts/matching-engine)](https://github.com/givingifts/matching-engine/blob/master/LICENSE)  
<div align="center">
  https://givin.gifts
</div>

# Matching Engine

<!-- PROJECT LOGO -->
<br/>
<div align="center">
  <a href="https://givin.gifts/">
    <img src="https://cdn.givin.gifts/assets/logo.png" alt="Matching" width="80" height="80">
  </a>

  <h3>Matching Engine</h3>

  <p>
    Matching engine used to run all exchanges in <a href="https://givin.gifts/">Givin.Gifts</a>.
    <br/>
    <a href="https://givin.gifts/contact">Contact us</a>
    ·
    <a href="https://github.com/givingifts/matching-engine">Report Bug</a>
    ·
    <a href="https://github.com/givingifts/matching-engine">Request Feature</a>
  </p>
</div>

## About the project
Matching engine is meant to match users according to different matching groups, with some extra logic for handling premium users and making sure that as many users as possible are matched close to each other
## Installation

If you want to tinker around with the project on your local PC, you can simply go ahead, clone the project and build it with Gradle.

```
git clone https://github.com/givingifts/matching-engine
```

```
./gradlew clean build
```

You can also build the 2 binaries by running

```
./make
```

To run the bot with the default configuration, you need to run the following command:
```
./make
./make run
```

In order to configure Matching engine, create a `matcher.yml` file.   
There you can set all your credentials.

## How it works
Matching its done in stages, where there is a "manager" and multiple "matchers" that can either be threads or separate instances.  
- The manager starts first, getting all matching groups, clearing the temp tables and doing some initialization  
- Premium matching goes first, doing a single round of matching of all groups except Worldwide.  
- We then apply the chosen premium behaviour for each user (match with non premium users, match with worldwide premium, drop from the exchange)
- After that we run worldwide premium matching
- We then cleanup any Premium user that is not being matched with non-premium users by dropping them from the exchange
- Matching is then done for each domestic group
- Some checks and quick fixes are apply after each matching, to match single users and try to make it so users send and receive from different users
- Any unmatched users are then promoted to their international group
- Matching is done for all international groups
- Any unmatched users are then promoted to Worldwide
- Matching is done for Worldwide
- At the end we generate some stats and checks for manual review!

## Built with

This project depends on the following projects, thanks to every developer who makes their code open-source! :heart:

- [Kotlin](https://kotlinlang.org/)
- [Exposed](https://github.com/JetBrains/Exposed) by [Jetbrains](https://github.com/JetBrains/)

## Contributing

You're very welcome to contribute to this project! Please note that this project uses [ktlint](https://github.com/pinterest/ktlint) to ensure consistent code.
It runs with `./gradlew clean build`, but you can also run it independently using `./gradlew ktlintCheck`.

## License

Distributed under the  AGPL-3.0 License. See `LICENSE.md` for more information.

### TODO List (PRs welcome!)
- Tests for each stage
- General cleanup
- Separate matching groups into batches that can be sent to different instances to speed up bigger groups like US

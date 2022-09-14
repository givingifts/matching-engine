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
- The manager then starts one instance of the matcher per matching group
- Each instance of the matcher will then get all users into memory, sorted randomly
- Each user will then be assigned a sequence
- Each user its checked to see they are not matched to someone in their do not match list according to the sequence
- If they are, we search for a swap with another user
- If no swap is possible, we drop them according to their preferences
- Once all users are matched, the result is written into the DB 

## Built with

This project depends on the following projects, thanks to every developer who makes their code open-source! :heart:

- [Kotlin](https://kotlinlang.org/)
- [Exposed](https://github.com/JetBrains/Exposed) by [Jetbrains](https://github.com/JetBrains/)

## Contributing

You're very welcome to contribute to this project! Please note that this project uses [ktlint](https://github.com/pinterest/ktlint) to ensure consistent code.
It runs with `make build`, but you can also run it independently using `make run`.

## License

Distributed under the  AGPL-3.0 License. See `LICENSE.md` for more information.

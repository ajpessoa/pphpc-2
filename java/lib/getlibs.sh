#!/usr/bin/env bash

JCOMMANDER_VER=1.47
UNCOMMONS_MATHS_VER=1.2.3
COMMONS_RNG_VER=1.5
BETTER_RANDOM_VER=5.6.2
PRNGINE_VER=2.0.0

JCOMMANDER=jcommander-${JCOMMANDER_VER}.jar
UNCOMMONS_MATHS=uncommons-maths-${UNCOMMONS_MATHS_VER}.jar
COMMONS_RNG_CLIENT=commons-rng-client-api-${COMMONS_RNG_VER}.jar
COMMONS_RNG_CORE=commons-rng-core-${COMMONS_RNG_VER}.jar
COMMONS_RNG_SIMPLE=commons-rng-simple-${COMMONS_RNG_VER}.jar
BETTER_RANDOM=BetterRandom-${BETTER_RANDOM_VER}.jar
PRNGINE=prngine-${PRNGINE_VER}.jar


if [ ! -e ${JCOMMANDER} ]
then
	wget https://repo.maven.apache.org/maven2/com/beust/jcommander/${JCOMMANDER_VER}/${JCOMMANDER}
else
	echo "JCommander already present, not downloading."
fi

if [ ! -e ${UNCOMMONS_MATHS} ]
then
	wget https://repo.maven.apache.org/maven2/io/gatling/uncommons/maths/uncommons-maths/${UNCOMMONS_MATHS_VER}/${UNCOMMONS_MATHS}
else
	echo "Uncommons maths already present, not downloading."
fi

if [ ! -e ${COMMONS_RNG_CLIENT} ]
then
	wget https://repo.maven.apache.org/maven2/org/apache/commons/commons-rng-client-api/${COMMONS_RNG_VER}/${COMMONS_RNG_CLIENT}
else
	echo "Apache Commons RNG Client API already present, not downloading."
fi

if [ ! -e ${COMMONS_RNG_CORE} ]
then
	wget https://repo.maven.apache.org/maven2/org/apache/commons/commons-rng-core/${COMMONS_RNG_VER}/${COMMONS_RNG_CORE}
else
	echo "Apache Commons RNG Core already present, not downloading."
fi

if [ ! -e ${COMMONS_RNG_SIMPLE} ]
then
	wget https://repo.maven.apache.org/maven2/org/apache/commons/commons-rng-simple/${COMMONS_RNG_VER}/${COMMONS_RNG_SIMPLE}
else
	echo "Apache Commons RNG Simple already present, not downloading."
fi

if [ ! -e ${BETTER_RANDOM} ]
then
	wget https://repo.maven.apache.org/maven2/io/github/pr0methean/betterrandom/BetterRandom/${BETTER_RANDOM_VER}/${BETTER_RANDOM}
else
	echo "Better Random already present, not downloading."
fi

if [ ! -e ${PRNGINE} ]
then
	wget https://repo.maven.apache.org/maven2/io/jenetics/prngine/${PRNGINE_VER}/${PRNGINE}
else
	echo "PRNGine already present, not downloading."
fi


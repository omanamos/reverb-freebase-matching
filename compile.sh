if [ ! -d ~/reverb-freebase-matching/bin ]; then
    mkdir ~/reverb-freebase-matching/bin
fi

javac -Xlint -cp "./lib/dist/*" -d ./bin `find src -name "*.java"`

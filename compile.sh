if [ ! -d ~/reverb-freebase-matching/bin ]; then
    mkdir ~/reverb-freebase-matching/bin
fi

javac -cp "./lib/*" -d ./bin `find src -name "*.java"`

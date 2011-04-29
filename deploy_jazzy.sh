cd lib/jazzy
emacs -nw src/com/swabunga/spell/engine/configuration.properties
ant library-core
cd ../
cp jazzy/dist/lib/jazzy-core.jar dist/
cd ../
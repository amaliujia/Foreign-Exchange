#jar cvf /Users/amaliujia/Downloads/libsvm-3.20/java/midterm.jar /Users/amaliujia/Documents/github/Foreign-Exchange/target/classes/Midterm/*.class
#java -classpath midterm.jar Solution /Users/amaliujia/Documents/CMU/Fall2015/11676/midterm/train2 /Users/amaliujia/Downloads/libsvm-3.20/java/FV /Users/amaliujia/Downloads/libsvm-3.20/java/FV2 
java -classpath libsvm.jar svm_scale -s range FV > train.scale
java -classpath libsvm.jar svm_scale -r range FV2 > test.scale
java -Xmx4g -classpath libsvm.jar svm_train -s 0 -c 5 -t 2 -g 0.5 -m 600 train.scale
java -Xmx4g -classpath libsvm.jar svm_predict test.scale train.scale.model result

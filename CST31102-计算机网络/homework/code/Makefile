
project: func.c  tools.o # Compile all the files of the entire project
	gcc tools.o func.c -o func -lpthread
	gcc -c tools.c 

runtest: func.c tools.c # Compile the program with the detailed output and run
	gcc -c tools.c
	gcc tools.o func.c -o func -lpthread
	sudo ./func

clean:
	rm func
	rm tools.o
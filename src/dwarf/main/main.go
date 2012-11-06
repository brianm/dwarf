package main

import (
	"dwarf"
	"os"
	"os/signal"
	"fmt"
	"flag"
)

func main() {
	var child_count int
	var quiet bool
	flag.IntVar(&child_count, "c", 1, "number of child processes")
	flag.BoolVar(&quiet, "q", false, "quiet -- suppress output")
	flag.Parse()
	args := flag.Args()

	waits := make([]chan int, 0)
	controls := make([]chan os.Signal, 0)
	for i := 0; i < child_count; i++ {

		var sout dwarf.PrintFunction 
		if quiet {
			sout = nil
		} else {
			sout = fmt.Printf
		}
		
		wait, control, err := dwarf.Spawn(sout, fmt.Printf, args[0], args[1:]...)
		if err != nil {
			panic(err)
		}
		waits = append(waits, wait)
		controls = append(controls, control)
	}

	sig := make(chan os.Signal, 1)
	signal.Notify(sig, os.Interrupt)
	go func() {
		// pass signal on to child
		signal := <- sig
		for _, c := range controls {
			c <- signal
		}

		fmt.Println("WHACK!")
	}()

	for _, w := range waits {
		<- w
	}

	fmt.Println("DONE!")
}

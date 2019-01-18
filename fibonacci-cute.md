Fibonacci cute

Lately I was implementing a general task processing engine and people asked me if I can put in some retry strategies. The system is well elaborated, but long text going short - I have just found a cute way for calculating fibonacci. Actually because I have asked a question for a programming competition for calculating fibonacci (without telling what they need to calculate) now I have a lot of funny methods for it! Read more if you want to see the non-recursive, directly expressed math version and also a little code for the inverse of the function.

The aim and the inspiration
===========================

First let me elaborate on the engine of mine as it worth some words by itself - actually it would worth its own post I think. Basically tasks are able to run into errors. I have a concept of a task runner that can get registered and they can throw exceptions. The interesting phenomena of course is that some errors are kind of worth a retry later (like no connection to the server) so the workers should throw an exception and tell that this task can be picked up later anytime on any node and thread and task runner to give it a try again. This is all well, but we have also found out that the multi-tasked and distributed engine is well fast enough to "DOS down" other systems so there should be some control over this. I have added two fields for the task entity:

- first time a retry happaned
- next time a retry can happen

The task engine now only picks out tasks where the next retry time is null (first try) or less than the current time. There is a nicely done architecture for not letting nodes pick out tasks at many places (actually they try but there is some optimistic locking that made later ones fail early and without problems) and everything is quite robust so little time insynchronities are not making a big deal, nor summer/winter time here so I decided to make these values of the entities more human readable. With the thing I have implemented a small interface for various strategies and a reference implementation that gives up trying after a threshold and has a configurable minimal time between retries. So good so far.

Then I was starting to think: "What if I am going to make something that grows exponentially or like a fibonacci?" Of course that is a good thing to have! Exponential implementation is easy, however the fibonacci seems that it has some implementation hurdles in the current setup! It needs the last and the last-before times (as you can calculate fib values out of that), but here I only have the time the retry happened the last time (if we went to run the task and it failed once again, then the last time is the next-time value from the entity as it has passed)! I could have iterate over the values in the series to find where we are, but first I did a little thinking.

I have found patterns in the thing and I started to believe that the function generating the sequence (the fib(n) function in the math-sense) can be inverted. I did not knew how, but it was at that point felt very much so I started looking for methods around fib. Then I have found a constant expression for it! Wow, then it is indeed invertable, just it is not that easy! All I need to do then is to:

- Get the first time + giveUp time and see if we should give up
- Get the last time (from the next time property after this failure)
- Calculate last - first in the time units we use
- Add one time unit to this (that is architectural)
- This is basically fib(n)*multiplier, where the multiplier is in my time units I use!
- Then I just divide this with the multiplier and get the fib(n)
- I do the fib^-1(fibn) on this to get last n
- calculate fib(n+1)*multiplier
- save it

This is as easy as it is written and I do not need to add various extra columns to entities, do not need to refactor legacy code and it fits so well. Also the constant expression I have found is surprisingly using the value of the golden ration! I like the whole thing as everyone likes that ratio haha! Also actually I am only approximating so I do something that so closely resemble the fibonacci that it is acting like that same. This is only necessary because we have some rounding and floating point calculation here and there and because I do not want to use a full-blown brutal math library or something and for my goals an approximation is well more than enough I just used some tricks. Actually for calculating the real inverse, I would need to solve an equation where the unknown 'n' is in the exponent of two different values and I was so lazy to solve it I just removed one of the values as it was less than 1/2 so just like how I removed it while the rounding in how I calculate the fib, I just ignored it in the inverse approximation. Now it is small, working and fast and everything. See the short C++:

	#include <cmath>
	#include <cstdio>

	static const double golden_ratio = (1 + sqrt(5.0)) / 2.0;

	// Calculates lob_b(a)
	double logb(double b, double a) {
		return log2(a) / log2(b);
	}

	// Calculate fibonacci by the closed form expression
	int fib(int n) {
		return (int)(0.5 + (pow(golden_ratio, n+1) /
					sqrt(5.0)));
	}

	// Approximates n if fib(n) is given as parameter
	int invfib(int fibn) {
		return (int)(logb(golden_ratio, sqrt(5.0)*(fibn)));
	}

	// Entry point
	int main() {
		for(int n = 0; n < 42; ++n) {
			printf("%dth: %d ; inv: %d\n", n, fib(n), invfib(fib(n)));
		}
		return 0;
	}

This is really enough to grasp the algoritm. Cute isn't it? I think it's really cute :)

I hope this is easy enough for non-c++ people too, but if you only speak java (or better: you want to see the stripped down version of the real code), [just go and download the original file for this.][0]

Final thoughts
==============

I did not know about this variant, and according to the [wikipedia article][1] most series that are recursive have this static formula which is great to know. I always liked to find or find out alternative algoritms and even though practically this is not really a breakthrough or anything in our code base - certainly it is a cute and kind solution.

[0]: http://ballmerpeak.web.elte.hu/devblog/attachments/FibonacciTimedTaskRetryStrategyDescriptor.java
[1]: https://en.wikipedia.org/wiki/Fibonacci_number#Closed-form_expression

Tags: fibonacci, math, alternative, algorithms, java, c++

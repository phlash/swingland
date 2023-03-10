# Java Swing re-implemented for Wayland

Yes, I'm completely mad.

Yes, I know about Wakefield, it's been in progress for years... stalled.

Yes, I know about XWayland - it's horribly broken for Swing applications (try one!) and is intended as a stop gap, not a long term solution.

## OK.. how?

By re-using the _interfaces_ of Swing, and providing completely new _implementations_ that lean on Wayland and maybe a component toolkit..

Presently, we build a new module / package `com.ashbysoft.swingland`, so client code will have to adjust imports to use us - this is by design
and also avoids horrible pain in replacing a system module (`java.desktop`) which is apparantly impossible without rebuilding the whole JDK..

## Build and test

I've chosen to use [Ant](https://ant.apache.org) as it provides just enough build tooling, is self contained, plus I know how to drive it.

Build/clean:
```bash
% ant [clean]
.....
```

Test:
```bash
% ant test
.....
```

The current test program is a very simple GUI application that extends a `Component` and draws itself directly in the `paint()` method.
I will at some point create a better one.

## Licence

See [LICENCE.md](LICENCE.md)

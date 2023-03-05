# Java Swing re-implemented for Wayland

Yes, I'm completely mad.

Yes, I know about Wakefield, it's been in progress for years... stalled.

Yes, I know about XWayland - it's horribly broken for Swing applications (try one!) and is intended as a stop gap, not a long term solution.

## OK.. how?

By re-using the _interfaces_ of Swing, and providing completely new _implementations_ that lean on Wayland and maybe a component toolkit..

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

## Licence

See [LICENCE.md](LICENCE.md)

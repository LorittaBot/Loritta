# Bliss

> I find I'm here this place of bliss

Bliss is a htmx-like framework made in Kotlin/JS.

It is *heavily* inspired by htmx, made just for fun and because I like coding... and reinventing the wheel. :)

By default, Bliss requests do not swap anything on the page. You need to be explicit about *what* you want to swap.

This example does execute the `/magic` request, but it won't do any swaps on the page.

```html
<button bliss-get="/magic">Click me!</button>

<div id="output"></div>
```

So, let's swap!

To swap, we need to choose *which* http status codes are allowed to be swapped, and *what* to swap.

```html
<button bliss-get="/magic" bliss-swap:200="body (innerHTML) -> #output (innerHTML)">Click me!</button>

<div id="output"></div>
```

This way any status response by the server that does not match what you expect will not be swapped.

## SSE (Server Sent Events)

For SSE, the SSE swaps are controlled by the server, not the client, to make things easier.
## BLOG documentation

We write our documentation in Markdown, and use
[Pelican](http://docs.getpelican.com/) to convert it to HTML.
This static HTML content can then be hosted anywhere.

The documentation consists of a set of pages located in `content/pages`.
When you type `make html`, Pelican will generate HTML in the `output` dir.
(This dir is deliberately not stored in git.) You can then take the content in
`output` and host it anywhere.

When writing documentation, you don't want to type `make html` all the time.
Instead, learn how to use Pelican's [development
server](http://docs.getpelican.com/en/3.3.0/getting_started.html#make).

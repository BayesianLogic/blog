# BLOG documentation

We write our documentation in Markdown, and use
[Pelican](http://docs.getpelican.com/) to convert it to HTML.
This static HTML content can then be hosted anywhere. 
To install Pelican, 
```
$ pip install pelican
```
or 
```
$ easy_install pelican
```

The documentation consists of a set of pages located in `content/pages`.
When you type `make html`, Pelican will generate HTML in the `output` dir.
(This dir is deliberately not stored in git.) You can then take the content in
`output` and host it anywhere.

When writing documentation, you don't want to type `make html` all the time.
Instead, learn how to use Pelican's [development
server](http://docs.getpelican.com/en/3.3.0/getting_started.html#make).

# Pages
All BLOG documents are under `docs/content/pages/`. Including
- index.md   
  main entry point of website.
- document.md
  main container for all user documents.
- user-manual.md
- get-start.md
- download.md
- release-note.md
  Please update right with every release.

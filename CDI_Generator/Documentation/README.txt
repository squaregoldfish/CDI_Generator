To generate HTML:

pandoc -s -S -t html5 --mathml --toc -c design.css -o <file>.html <file>.txt

For a LaTeX-based PDF:

pandoc -s <file>.txt -o <file>.pdf

For a Word document:

pandoc -s -S <file>.txt -o <file>.docx

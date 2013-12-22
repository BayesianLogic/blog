;;; blog-mode.el --- major mode for editing BLOG code
;;; Language: blog
;;; Maintainer: Wei WANG
;;; Created: July 31, 2013
;;; Last revised: August 1, 2013
;;; Latest Revision: blog 0.6

;;;add the following lines in .emacs to activate blog-mode
;;;(require 'blog-mode)
;;;(add-to-list 'auto-mode-alist '("\\.blog\\'" . blog-mode))
;;;(add-to-list 'auto-mode-alist '("\\.dblog\\'" . blog-mode))


;; define several class of keywords
(setq blogKeys (regexp-opt '("type" "distinct" "random" "origin" "fixed" "param" "query" "obs" "guaranteed"
			     ) 'words))
(setq blogTypes (regexp-opt '("Real" "R3Vector" "Timestep" "Boolean" "Integer" "String"
			      ) 'words))
(setq blogDistributions (regexp-opt '("TabularCPD" "Dirichlet" "UniformVector" "UniformReal" "MultivarGaussian"
				      "Poisson" "UnivarGaussian" "Gaussian" "Categorical" "UniformChoice"
				      "Bernoulli" "Geometric"
				      ) 'words))
(setq blogBasicStatements (regexp-opt '("if" "then" "else" "null" "true" "false" "forall" "exists" "for"
				  ) 'words))

(setq blogNumberStatement "#[^\s\]*") ; #TypeName
(setq blogListOrArrayStement "[[].*[]]") ; [ ... ]
(setq blogTimeStatement "@[0-9]+") ; @0 @1 ...
(setq blogSetStatement "{[^{}]*}")

;; keywords for syntax coloring
(setq blog-keywords
      `(
	( ,blogKeys . font-lock-preprocessor-face)
	( ,blogTypes . font-lock-type-face)
	( ,blogDistributions . font-lock-function-name-face)
	( ,blogBasicStatements . font-lock-keyword-face)
	( ,blogNumberStatement . font-lock-constant-face)
	( ,blogListOrArrayStement . font-lock-constant-face)
	( ,blogTimeStatement . font-lock-constant-face)
	( ,blogSetStatement  . font-lock-constant-face)
        )
      )

;; syntax table
(defvar blog-syntax-table nil "Syntax table for `blog-mode'.")
(setq blog-syntax-table
      (let ((synTable (make-syntax-table)))

        ;; c++ style comment: “// …” and “/* … */” 
	(modify-syntax-entry ?\/  ". 124b" synTable)
	(modify-syntax-entry ?*  ". 23" synTable)
	(modify-syntax-entry ?\n  "> b" synTable)
        synTable))

;; command to comment/uncomment text
(defun blog-comment-dwim (arg)
  "Comment or uncomment current line or region in a smart way.
For detail, see `comment-dwim'."
  (interactive "*P")
  (require 'newcomment)
  (let (
        (comment-start "//") (comment-end "")
        )
    (comment-dwim arg)))

;; define the major mode.
(define-derived-mode blog-mode perl-mode ;;fundamental-mode
  "blog-mode is a major mode for editing language blog."
  :syntax-table blog-syntax-table
  
  (setq font-lock-defaults '(blog-keywords))
  (setq mode-name "blog")

  ;; modify the keymap
  (define-key blog-mode-map [remap comment-dwim] 'blog-comment-dwim)
)

(provide 'blog-mode)

;;; blog-mode.el ends here
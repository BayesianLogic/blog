Please copy all the files to the Sublime package directory:
On MacOSx it is 
~/Library/Application Support/Sublime Text 3/Packages/User
or 
~/Library/Application Support/Sublime Text 2/Packages/User


# How to modify the syntax

## Make sure PackageControl and AAAPackageDev are installed

### PackageControl
The following works for Sublime Text 3, press `Ctrl+\``, then copy paste the following
```
import urllib.request,os,hashlib; h = '7183a2d3e96f11eeadd761d777e62404' + 'e330c659d4bb41d3bdf022e94cab3cd0'; pf = 'Package Control.sublime-package'; ipp = sublime.installed_packages_path(); urllib.request.install_opener( urllib.request.build_opener( urllib.request.ProxyHandler()) ); by = urllib.request.urlopen( 'http://sublime.wbond.net/' + pf.replace(' ', '%20')).read(); dh = hashlib.sha256(by).hexdigest(); print('Error validating download (got %s instead of %s), please try manual install' % (dh, h)) if dh != h else open(os.path.join( ipp, pf), 'wb' ).write(by)
```

### Install AAAPackageDev
Preferences -> Package Control -> Install Package -> AAAPackageDev

## Modifying `blog.YAML-tmLanguage`
  then `Cmd + B`
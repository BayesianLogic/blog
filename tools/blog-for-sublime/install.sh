#!/bin/sh

# install BLOG highlighting for sublime

platform=`uname`
if [ $platform = "Linux" ]; then
  if [ -d "~/.config/sublime-text-2" ]; then 
  	cp blog.* ~/.config/sublime-text-2/Packages/User/
  else
    cp blog.* ~/.config/sublime-text-3/Packages/User/
  fi
elif [ $platform = "Darwin" ]; then
    if [ -d "~/Library/Application\ Support/Sublime\ Text\ 2/Packages/User" ]; then 
  	  cp blog.* ~/Library/Application\ Support/Sublime\ Text\ 2/Packages/User/
    else
      cp blog.* ~/Library/Application\ Support/Sublime\ Text\ 3/Packages/User/
    fi
else
	echo "Windows installation not supported"
	echo "You have to manually copy blog.* into your sublime package user directory" 
fi

echo "Install BLOG syntax highlighting for Sublime Text completed"

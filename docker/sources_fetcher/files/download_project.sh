#!/bin/bash
#
# This file is part of symfinder.
#
# symfinder is free software: you can redistribute it and/or modify
# it under the terms of the GNU Lesser General Public License as published by
# the Free Software Foundation, either version 3 of the License, or
# (at your option) any later version.
#
# symfinder is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
# GNU Lesser General Public License for more details.
#
# You should have received a copy of the GNU Lesser General Public License
# along with symfinder. If not, see <http://www.gnu.org/licenses/>.
#
# Copyright 2018-2020 Johann Mortara <johann.mortara@univ-cotedazur.fr>
# Copyright 2018-2020 Xhevahire Tërnava <xhevahire.ternava@lip6.fr>
# Copyright 2018-2020 Philippe Collet <philippe.collet@univ-cotedazur.fr>
#

# $1: repository URL
# $2: current project directory path
download_project(){
    git clone $1 $2
}

# $1: project directory
# ${@:2}: desired commits to checkout
checkout_commits(){
	for i in "${@:2}"; do
	directory_path=$1-${i/\//_}/
        git --git-dir=$1/.git --work-tree=$1 checkout ${i}
        if [ ! -d $directory_path ]; then
            mkdir -p $directory_path
            cp -r $1/* $directory_path
        fi
    done
}

# $1: project directory
# ${@:2}: desired tags to checkout
checkout_tags(){
	for i in "${@:2}"; do
	directory_path=$1-${i/\//_}/
        git --git-dir=$1/.git --work-tree=$1 checkout tags/${i}
        if [ ! -d $directory_path ]; then
            mkdir -p $directory_path
            cp -r $1/* $directory_path
        fi
    done
}

# $1: current project directory path
delete_project(){
    rm -rf $1
}

case "$1" in
	"download")
    	download_project ${@:2}
        ;;
	"commit")
    	checkout_commits ${@:2}
        ;;
    "tag")
        checkout_tags ${@:2}
        ;;
    "delete")
        delete_project ${@:2}
        ;;
esac
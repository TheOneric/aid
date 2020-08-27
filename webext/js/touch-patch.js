/*
    This file is part of AiD.

    AiD is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    AiD is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with AiD.  If not, see <http://www.gnu.org/licenses/>.

    Copyright 2020  Oneric
*/
'use strict';

window.addEventListener('beforescriptexecute',
    function(event)
    {
        if(event.target.src.match(/\/assets\/application\/application-[0-9a-f]+.js$/)) {
            aid_log_info("Attemp to apply Touch-Patch …");
            var pnode = event.target.parentNode;
            pnode.removeChild(event.target);
            event.preventDefault();

            fetch(event.target.src)
                .then(r => r.text(), aid_log_err)
                .then(src => src.replace(
                    'function t(t){if(i.preventDefaultEvents&&t.preventDefault(),s){var n=t.touches[0].pageX,o=t.touches[0].pageY,l=r-n,u=a-o;Math.abs(l)>=i.min_move_x?(e(),l>0?i.wipeLeft():i.wipeRight()):Math.abs(u)>=i.min_move_y&&(e(),u>0?i.wipeDown():i.wipeUp())}}',
                    `
                    //----BEGIN-CHANGES----//
                    function t(t){
                        //aid_log_dbg("¡DEBUG-MSG!");
                        //if(i.preventDefaultEvents&&t.preventDefault(),s){
                            var n=t.touches[0].pageX,o=t.touches[0].pageY,l=r-n,u=a-o;Math.abs(l)>=i.min_move_x?(e(),l>0?i.wipeLeft():i.wipeRight()):Math.abs(u)>=i.min_move_y&&(e(),u>0?i.wipeDown():i.wipeUp())
                        //}
                    }
                    //-----END-CHANGES-----//
                    `,
                    aid_log_err
                ))
                .then(new_src => {
					window.eval(new_src);
                    aid_log_info("Touch-Patch applied.")
				 }, aid_log_err);
                /*Need eval to expose defined objects and functions (which probably exists,
                  but I wont read 1.3MiB of minified code to find out)
                  However since this script is loaded from AoD and we just applied a small
                  and sane patch, this should be fine.
                */
        }
    }
);

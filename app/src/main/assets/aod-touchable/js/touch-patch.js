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
                        //console.log("¡DEBUG-MSG!");
                        //if(i.preventDefaultEvents&&t.preventDefault(),s){
                            var n=t.touches[0].pageX,o=t.touches[0].pageY,l=r-n,u=a-o;Math.abs(l)>=i.min_move_x?(e(),l>0?i.wipeLeft():i.wipeRight()):Math.abs(u)>=i.min_move_y&&(e(),u>0?i.wipeDown():i.wipeUp())
                        //}
                    }
                    //-----END-CHANGES-----//
                    `,
                    aid_log_err
                ))
                .then(src => src.replace(
                    'function p(e){if(!_&&t.settings.swipePropagation&&f(e),!w&&_&&h(e)){var i=d(e),n={pageX:i.pageX,pageY:i.pageY},r=n.pageX-m.pageX,a=n.pageY-m.pageY;l(r,a),m=n;var s=(new Date).getTime(),u=s-v;u>0&&(y.x=r/u,y.y=a/u,v=s),o(r,a)&&(e.stopPropagation(),e.preventDefault())}}',
                    `
                    function p(event) {
                      if(!_&&t.settings.swipePropagation&&f(event),!w&&_&&h(event)) {
                        var i=d(event),
                        n={pageX:i.pageX,pageY:i.pageY},
                        r=n.pageX-m.pageX,
                        a=n.pageY-m.pageY;
                        l(r,a),
                        m=n;
                        var s=(new Date).getTime(),
                        u=s-v;
                        u>0 && (y.x=r/u,y.y=a/u,v=s);
                        o(r,a);
                        //o's return val is used by AoD to decide whether or not to cancel the event
                        // But it is to unreliable to use on touch devices

                        //e comes from the super function i(e, t, i, n) and is what is being used in o(a1, a2)
                        // function i envelops functions o,l,u,c,d,h,f,p
                        if(
                           !(a>=0&&e.scrollTop==0 || a<=0&&e.scrollTop+1>=(t.contentHeight-t.containerHeight))
                          ) {
                          event.stopPropagation();
                          event.preventDefault();
                        }

                         /*console.log(
                           "hi: "+a+"   "+t.contentHeight+" - "+t.containerHeight+" : "+e.scrollTop
                         ); console.log(
                           "BothNeg: " + (!(a>=0&&e.scrollTop==0 || a<=0&&e.scrollTop+2>=(t.contentHeight-t.containerHeight)))
                         );*/
                      }
                    }
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

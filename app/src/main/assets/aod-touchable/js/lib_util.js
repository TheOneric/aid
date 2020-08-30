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


const AID_DBG  = 0;
const AID_INFO = 1;
const AID_WARN = 2;
const AID_ERR  = 3;

//TODO: make runtime option
const AID_MIN_MSG_LVL = 0;

function aid_get_msg_prefix(lvl) {
    switch(lvl) {
        case AID_DBG:
            return "[DEBUG]";
        case AID_INFO:
            return "[INFO]";
        case AID_WARN:
            return "[WARNING]";
        case AID_ERR:
            return "[ERROR]";
        default:
            return "[MYSTERY]";
    }
}

function aid_log(lvl, msg) {
    if(lvl > AID_MIN_MSG_LVL)
        console.log("[AiD]"+aid_get_msg_prefix(lvl)+": "+msg);
}

// Shorthands
function aid_log_dbg (msg) { aid_log(AID_DBG,  msg); }
function aid_log_info(msg) { aid_log(AID_INFO, msg); }
function aid_log_warn(msg) { aid_log(AID_WARN, msg); }
function aid_log_err (msg) { aid_log(AID_ERR,  msg); }

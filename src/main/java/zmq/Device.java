/*
    Copyright (c) 2007-2011 iMatix Corporation
    Copyright (c) 2007-2011 Other contributors as noted in the AUTHORS file

    This file is part of 0MQ.

    0MQ is free software; you can redistribute it and/or modify it under
    the terms of the GNU Lesser General Public License as published by
    the Free Software Foundation; either version 3 of the License, or
    (at your option) any later version.

    0MQ is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Lesser General Public License for more details.

    You should have received a copy of the GNU Lesser General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
package zmq;

public class Device {
    
    public static boolean device (SocketBase insocket_,
            SocketBase outsocket_)
    {

        //  The algorithm below assumes ratio of requests and replies processed
        //  under full load to be 1:1.

        //  TODO: The current implementation drops messages when
        //  any of the pipes becomes full.

        boolean success;
        int rc;
        int more;
        Msg msg;
        PollItem items [] = new PollItem[2];
        
        items[0] = new PollItem (insocket_, ZMQ.ZMQ_POLLIN );
        items[1] = new PollItem (outsocket_, ZMQ.ZMQ_POLLIN );
        
        while (true) {
            //  Wait while there are either requests or replies to process.
            rc = ZMQ.zmq_poll (items, -1);
            if (rc < 0)
                return false;

            //  Process a request.
            if (items [0].isReadable()) {
                while (true) {
                    msg = insocket_.recv (ZMQ.ZMQ_DONTWAIT);
                    if (msg == null)
                        break;

                    more = insocket_.getsockopt (ZMQ.ZMQ_RCVMORE);

                    success = outsocket_.send (msg, more > 0? ZMQ.ZMQ_SNDMORE: 0);
                    if (!success)
                        return false;
                    if (more == 0)
                        break;
                }
            }
            //  Process a reply.
            if (items [1].isReadable()) {
                while (true) {
                    msg = outsocket_.recv (ZMQ.ZMQ_DONTWAIT);
                    if (msg == null)
                        break;

                    more = outsocket_.getsockopt (ZMQ.ZMQ_RCVMORE);

                    success = insocket_.send (msg, more > 0? ZMQ.ZMQ_SNDMORE: 0);
                    if (!success)
                        return false;
                    if (more == 0)
                        break;
                }
            }

        }
        
    }
}

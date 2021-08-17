/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.flink.runtime.leaderelection;

import javax.annotation.Nonnull;

import java.util.UUID;

/**
 * Interface for a service which allows to elect a leader among a group of contenders.
 *
 * <p>Prior to using this service, it has to be started calling the start method. The start method
 * takes the contender as a parameter. If there are multiple contenders, then each contender has to
 * instantiate its own leader election service.
 *
 * <p>Once a contender has been granted leadership he has to confirm the received leader session ID
 * by calling the method {@link #confirmLeadership(UUID, String)}. This will notify the leader
 * election service, that the contender has accepted the leadership specified and that the leader
 * session id as well as the leader address can now be published for leader retrieval services.
 *
 * <p>Leader选举是通过LeaderElectionService(选举服务)和LeaderContender(参与竞选的对象)共同来完成的，
 * 每一次选举成功后都会有唯一的leaderSessionId，可以用来作为RpcGateway通信的fencing token.
 *
 * <p>当一个LeaderContender竞选成功了，会通过LeaderContender#grantLeadership得到通知.
 *
 * <p>LeaderElectionService有多种实现，如无需进行选举过程的StandaloneLeaderElectionService，以及默认实现
 * DefaultLeaderElectionService, EmbeddedLeaderElectionService等.
 */
public interface LeaderElectionService {

    /**
     * Starts the leader election service. This method can only be called once.
     *
     * @param contender LeaderContender which applies for the leadership
     * @throws Exception
     */
    void start(LeaderContender contender) throws Exception;

    /**
     * Stops the leader election service.
     *
     * @throws Exception
     */
    void stop() throws Exception;

    /**
     * Confirms that the {@link LeaderContender} has accepted the leadership identified by the given
     * leader session id. It also publishes the leader address under which the leader is reachable.
     *
     * <p>The rational behind this method is to establish an order between setting the new leader
     * session ID in the {@link LeaderContender} and publishing the new leader session ID as well as
     * the leader address to the leader retrieval services.
     *
     * @param leaderSessionID The new leader session ID
     * @param leaderAddress The address of the new leader
     */
    void confirmLeadership(UUID leaderSessionID, String leaderAddress);

    /**
     * Returns true if the {@link LeaderContender} with which the service has been started owns
     * currently the leadership under the given leader session id.
     *
     * @param leaderSessionId identifying the current leader
     * @return true if the associated {@link LeaderContender} is the leader, otherwise false
     */
    boolean hasLeadership(@Nonnull UUID leaderSessionId);
}

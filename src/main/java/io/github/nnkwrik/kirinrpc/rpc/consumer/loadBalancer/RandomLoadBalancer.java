package io.github.nnkwrik.kirinrpc.rpc.consumer.loadBalancer;

import io.github.nnkwrik.kirinrpc.netty.cli.KChannel;
import io.github.nnkwrik.kirinrpc.rpc.model.ServiceMeta;

import java.util.Random;

/**
 * @author nnkwrik
 * @date 19/06/01 16:38
 */
public class RandomLoadBalancer extends AbstractLoadBalancer {

    private Random rand = new Random();

    @Override
    protected KChannel doSelect(KChannel[] connectionArray, ServiceMeta service) {
        int length = connectionArray.length;
        int[] weightArray = new int[length];

        int totalWeight = 0;
        boolean sameWeight = true;
        for (int i = 0; i < length; i++) {
            KChannel connection = connectionArray[i];
            int weight = connection.getWeight(service);

            weightArray[i] = weight;
            sameWeight = sameWeight && (i == 0 || weight == weightArray[i - 1]);
            totalWeight += weight;
        }

        if (totalWeight > 0 && !sameWeight) {
            int offset = rand.nextInt(totalWeight);

            for (int i = 0; i < length; i++) {
                offset -= weightArray[i];
                if (offset < 0) {
                    return connectionArray[i];
                }
            }
        }

        return connectionArray[rand.nextInt(length)];
    }

}

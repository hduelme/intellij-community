// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.util.containers;

class ConcurrentPackedBitsArrayImpl implements ConcurrentPackedBitsArray {
  private final int bitsPerChunk;
  private final ConcurrentBitSetImpl bits = new ConcurrentBitSetImpl();
  private final int mask;
  private final int chunksPerWord;

  ConcurrentPackedBitsArrayImpl(int bitsPerChunk) {
    if (bitsPerChunk <= 0 || bitsPerChunk > ConcurrentBitSetImpl.BITS_PER_WORD) {
      throw new IllegalArgumentException(
        "Bits-to-pack number must be between 1 and " + ConcurrentBitSetImpl.BITS_PER_WORD + ", but got: " + bitsPerChunk);
    }
    this.bitsPerChunk = bitsPerChunk;
    mask = bitsPerChunk == Integer.SIZE ? -1 : (1 << bitsPerChunk) - 1;
    chunksPerWord = ConcurrentBitSetImpl.BITS_PER_WORD / bitsPerChunk;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public long get(int id) {
    assert id >= 0 : id;
    int bitIndex = id / chunksPerWord * ConcurrentBitSetImpl.BITS_PER_WORD + (id % chunksPerWord) * bitsPerChunk;
    return Integer.toUnsignedLong( (bits.getWord(bitIndex) >> bitIndex) & mask );
  }

  // stores chunk atomically, returns previous chunk
  @Override
  public long set(int id, long flags) {
    assert id >= 0 : id;
    if ((flags & ~mask) != 0) {
      throw new IllegalArgumentException("Flags must have <= " + bitsPerChunk + " bits set, but got: " + Long.toBinaryString(flags));
    }
    int bitIndex = id / chunksPerWord * ConcurrentBitSetImpl.BITS_PER_WORD + (id % chunksPerWord) * bitsPerChunk;

    int oldFlags = bits.changeWord(bitIndex, word -> word & ~(mask << bitIndex) | ((int)flags << bitIndex)) >> bitIndex;
    return Integer.toUnsignedLong(oldFlags & mask);
  }

  @Override
  public void clear() {
    bits.clear();
  }
}
